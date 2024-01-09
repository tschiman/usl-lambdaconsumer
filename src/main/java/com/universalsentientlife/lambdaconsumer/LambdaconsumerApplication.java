package com.universalsentientlife.lambdaconsumer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class LambdaconsumerApplication implements RequestHandler<String, String> {

	@Override
	public String handleRequest(String input, Context context) {
		context.getLogger().log("Input: " + input);
		// Your processing logic
		return "Hello, " + input;
	}

}
