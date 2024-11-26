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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LambdaconsumerApplication implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
		try {
			context.getLogger().log("Regions: " + System.getenv("AWS_REGION"));

			RestTemplate restTemplate = new RestTemplate();
			String uslBaseUrl = System.getenv("USL_URL");

			//process the body
			var objectMapper = new ObjectMapper();
			AwsChunkPayload awsPayload;
			try {
				awsPayload = objectMapper.readValue(input.getBody(), AwsChunkPayload.class);

				context.getLogger().log(awsPayload.getEmail());
				context.getLogger().log(awsPayload.getChunkNumber().toString());

				if (awsPayload.getFileName() == null) {
					//check auth and return
					context.getLogger().log("Starting Auth Request");

					HttpHeaders headers = new HttpHeaders();
					headers.setAccept(List.of(MediaType.APPLICATION_JSON));
					headers.setBasicAuth(awsPayload.getEmail(), awsPayload.getPassword());

					// Create an HttpEntity with headers
					HttpEntity<String> entity = new HttpEntity<>(headers);

					// Make the GET request
					ResponseEntity<FileAuthorizationResponse> response = restTemplate.exchange(
							uslBaseUrl + "/api/upload/auth?fileSizeInMB=" + URLEncoder.encode(awsPayload.getFileSizeInMB().toString(), StandardCharsets.UTF_8),
							HttpMethod.GET,
							entity,
							FileAuthorizationResponse.class
					);

					var result = new APIGatewayProxyResponseEvent();
					result.setBody(objectMapper.writeValueAsString(response.getBody()));
					result.setStatusCode(200);
					result.setHeaders(new HashMap<>());
					result.setIsBase64Encoded(false);
					result.setMultiValueHeaders(new HashMap<>());
					context.getLogger().log("Ending Auth Request");
					return result;
				}

				//login to USL web server
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.setBasicAuth(awsPayload.getEmail(), awsPayload.getPassword());

				// Create an HttpEntity with headers
				HttpEntity<String> entity = new HttpEntity<>(headers);

				// Make the GET request
				ResponseEntity<EncryptionResponse> response = restTemplate.exchange(
						uslBaseUrl + "/api/encryption",
						HttpMethod.GET,
						entity,
						EncryptionResponse.class
				);

				if (response.getBody() != null) {
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

							ObjectMetadata objectMetadata = new ObjectMetadata();
							objectMetadata.setContentLength(encryptedBytes.length);

							s3client.putObject(bucketName, objectKey, new ByteArrayInputStream(encryptedBytes), objectMetadata);
							context.getLogger().log("Successfully wrote to S3");

							if (awsPayload.isLastChunk()) {
								// Make the POST request
								headers = new HttpHeaders();
								headers.setContentType(MediaType.APPLICATION_JSON);
								headers.setBasicAuth(awsPayload.getEmail(), awsPayload.getPassword());

								// Create an HttpEntity with headers
								var body = new LastChunkDto();
								body.setFileName(awsPayload.getFileName());
								body.setFileCount(awsPayload.getChunkNumber());
								body.setFileSizeInMB(awsPayload.getFileSizeInMB());
								var entityLastChunk = new HttpEntity<>(body, headers);

								try {
									restTemplate.exchange(
											uslBaseUrl + "/api/metadata",
											HttpMethod.POST,
											entityLastChunk,
											LastChunkDto.class
									);
								} catch (Exception e) {
									try {
										restTemplate.exchange(
												uslBaseUrl + "/api/metadata",
												HttpMethod.POST,
												entityLastChunk,
												LastChunkDto.class
										);
									} catch (Exception e2) {
										try {
											restTemplate.exchange(
													uslBaseUrl + "/api/metadata",
													HttpMethod.POST,
													entityLastChunk,
													LastChunkDto.class
											);
										} catch (Exception e3) {
											context.getLogger().log("Failed sending file metadata");
											context.getLogger().log(e3.getMessage());
											context.getLogger().log(e3.getStackTrace().toString());
										}
									}
								}
							}
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
				} else {
					context.getLogger().log("User not subscribed email: " + awsPayload.getEmail());
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
