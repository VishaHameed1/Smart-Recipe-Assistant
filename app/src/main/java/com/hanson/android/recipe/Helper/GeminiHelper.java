//package com.hanson.android.recipe.Helper;
//
//import com.google.ai.client.generativeai.GenerativeModel;
//import com.google.ai.client.generativeai.java.GenerativeModelFutures;
//import com.google.ai.client.generativeai.type.Content;
//import com.google.ai.client.generativeai.type.GenerateContentResponse;
//import com.google.common.util.concurrent.ListenableFuture;
//
//public class GeminiHelper {
//    private GenerativeModelFutures model;
//
//    public GeminiHelper() {
//        // "gemini-1.5-flash" fast aur free hai
//        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", "AIzaSyA1dYTy1uFqiVz-UPeFJ9_XLL99OhPWgzg");
//        model = GenerativeModelFutures.from(gm);
//    }
//
//    public ListenableFuture<GenerateContentResponse> getResponse(String prompt) {
//        Content content = new Content.Builder().addText(prompt).build();
//        return model.generateContent(content);
//    }
//}
