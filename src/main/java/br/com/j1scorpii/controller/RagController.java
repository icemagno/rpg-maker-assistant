package br.com.j1scorpii.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.j1scorpii.service.RagService;

@RestController
@RequestMapping(value="/rag")
public class RagController {
	
	@Autowired private RagService ragService;

    @PostMapping( value="/ask", consumes= MediaType.APPLICATION_JSON_VALUE, produces= MediaType.APPLICATION_JSON_VALUE )
    public ResponseEntity<String> getAnswer( @RequestBody String data ) {
    	return new ResponseEntity<String>( ragService.getAnswer( data ) , HttpStatus.OK);
    }    
    
   
}
