package br.com.j1scorpii.service;

import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

@Service
@EnableScheduling
public class RagService {
	
	private RestTemplate rt;
	private String minervaAddress = "http://localhost:11434";
	private String minervaModel = "HammerAI/mythomax-l2:latest";
	
	@PostConstruct
	private void init() {
		this.rt = new RestTemplate();
	}
	
	
	public String getAnswer( String question ) {
		JSONObject tt = new JSONObject( question ).getJSONObject("data");
		String textToSearch = tt.getString("question");
				
		JSONArray messages = new JSONArray()
			.put(  
				new JSONObject()
					.put("role", "system")
					.put(
							"content", 
							"Responda a questão a seguir" 
					)
			)
			.put(
				new JSONObject()
					.put("role", "user")
					.put("content", textToSearch )
			);
		
		JSONObject modelQuery = new JSONObject()
				.put("model", minervaModel )
				.put("stream", false)
				.put("messages", messages );
		
		System.out.println( modelQuery.toString(5) );

		String answer = null;
		
		try {
			answer = chatToModel( modelQuery.toString() );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println( answer );
		
		return answer;
	}
	
	public String getEmbeddings( String text ) throws Exception {
		JSONObject payload = new JSONObject();
		payload.put("model", minervaModel );
		payload.put("prompt", text.toLowerCase() );
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		RequestEntity<String> requestEntity = RequestEntity 
				.post( new URL( minervaAddress + "/embeddings" ).toURI() ) 
				.contentType( MediaType.APPLICATION_JSON ) 
				.body( payload.toString() ); 
		return rt.exchange(requestEntity, String.class ).getBody();		
	}

	private String chatToModel( String text ) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		RequestEntity<String> requestEntity = RequestEntity 
				.post( new URL( minervaAddress + "/chat" ).toURI() ) 
				.contentType( MediaType.APPLICATION_JSON ) 
				.body( text ); 
		return rt.exchange(requestEntity, String.class ).getBody();		
	}	

}
