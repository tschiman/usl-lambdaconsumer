package com.universalsentientlife.lambdaconsumer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LambdaconsumerApplication implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
		try {
			context.getLogger().log("Regions: " + System.getenv("AWS_REGION"));

			RestTemplate restTemplate = new RestTemplate();
			String uslBaseUrl = "https://687f-2601-681-4500-5a60-d5b6-a775-58-5ddc.ngrok-free.app";

			//process the body
			var objectMapper = new ObjectMapper();
			AwsChunkPayload awsPayload;
			try {
				awsPayload = objectMapper.readValue(input.getBody(), AwsChunkPayload.class);

				context.getLogger().log(awsPayload.getEmail());
				context.getLogger().log(awsPayload.getChunkNumber().toString());

				//login to USL web server
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.set("X-USL-EMAIL", awsPayload.getEmail());
				headers.set("X-USL-PASSWORD", awsPayload.getPassword());

				// Create an HttpEntity with headers only
				HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
				ResponseEntity<AuthTokenDTO> result = restTemplate.postForEntity(
						uslBaseUrl + "/api/users/auth",
						requestEntity,
						AuthTokenDTO.class
				);

				String authToken = Objects.requireNonNull(result.getBody()).getToken();

				headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.set("X-USL-EMAIL", awsPayload.getEmail()); // Set your custom header here
				headers.set("X-USL-AUTH-TOKEN", authToken); // Set your custom header here

				// Create an HttpEntity with headers
				HttpEntity<String> entity = new HttpEntity<>(headers);

				// Make the GET request
				ResponseEntity<EncryptionResponse> response = restTemplate.exchange(
						uslBaseUrl + "/api/encryption",
						HttpMethod.GET,
						entity,
						EncryptionResponse.class
				);

				//we should encrypt the payload
				byte[] chunk = awsPayload.getChunk();
				byte[] encryptedBytes;
				try {
					encryptedBytes = AesEncryptionUtil.encryptAES(chunk, Objects.requireNonNull(response.getBody()).getEncryptionKey());
					context.getLogger().log("Chunk size: " + chunk.length + " encryptedChunk size: " + encryptedBytes.length);

					//then store the payload in s3
					try {
						AmazonS3 s3client = AmazonS3ClientBuilder.standard().build();
						String bucketName = "universalsentientlife";
						String objectKey = awsPayload.getEmail() + "/" + response.getBody().getSalt() + "/" + awsPayload.getFileName() + "/" + awsPayload.getChunkNumber();
						String val = "input";
						ObjectMetadata objectMetadata = new ObjectMetadata();
						objectMetadata.setContentLength(encryptedBytes.length);
						s3client.putObject(bucketName, objectKey, new ByteArrayInputStream(encryptedBytes), objectMetadata);
						context.getLogger().log("Successfully wrote to S3");
					} catch (Exception e) {
						context.getLogger().log("Failed S3");
						context.getLogger().log(e.getMessage());
						context.getLogger().log(e.getStackTrace().toString());
					}


				} catch (GeneralSecurityException e) {
					context.getLogger().log("Encryption error");
					context.getLogger().log(e.getMessage());
					context.getLogger().log(e.getStackTrace().toString());
				}

			} catch (JsonProcessingException e) {
				context.getLogger().log("Failed to map aws payload with jackson");
				context.getLogger().log(e.getMessage());
				context.getLogger().log(e.getStackTrace().toString());
			}
		} catch (Exception e) {
			context.getLogger().log("Unexpected error");
			context.getLogger().log(e.getMessage());
			context.getLogger().log(e.getStackTrace().toString());
		}


		// Your processing logic

		var response = new APIGatewayProxyResponseEvent();
		response.setBody("Hello, " + input.getBody());
		response.setStatusCode(200);
		response.setHeaders(new HashMap<>());
		response.setIsBase64Encoded(false);
		response.setMultiValueHeaders(new HashMap<>());
		return response;
	}



}
