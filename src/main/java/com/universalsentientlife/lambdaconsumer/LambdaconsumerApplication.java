package com.universalsentientlife.lambdaconsumer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class LambdaconsumerApplication implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
		context.getLogger().log("Input: " + input);
		context.getLogger().log("Input Body: " + input.getBody());
		context.getLogger().log("Regions: " + System.getenv("AWS_REGION"));

		//process the body
		var objectMapper = new ObjectMapper();
		AwsChunkPayload awsPayload;
		try {
			awsPayload = objectMapper.readValue(input.getBody(), AwsChunkPayload.class);
			context.getLogger().log(awsPayload.getEmail());
			context.getLogger().log(awsPayload.getChunkNumber().toString());
		} catch (JsonProcessingException e) {
			context.getLogger().log("Failed to map aws payload");
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
