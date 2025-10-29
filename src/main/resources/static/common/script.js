
var Toast = Swal.mixin({
	toast: true,
	position: 'top-end',
	showConfirmButton: false,
	timer: 5000
});


function alertToast( title, text, icon = 'success' ){
	Toast.fire({
	  	title: title,
	  	text: text,
	  	icon: icon
	});	
}

$( document ).ready(function() {

	$("#askBtn").click( ()=>{
		let textQuestion = $("#msgText").val();
		if ( !textQuestion ) return;
		let data = { question: textQuestion }

		alertToast( "Aguarde...", "Isso pode demorar um pouco. Não saia dessa página...", icon = 'warning' )

		putMyText( textQuestion );
		$("#msgText").val('');
		$.ajax ({
		    url: "/rag/ask",
		    type: "POST",
		    data: JSON.stringify( {data : data } ),
		    dataType: "json",
		    contentType: "application/json; charset=utf-8",
		    success: function( data ){
				console.log( data );
				putHisText( data.message.content)
		    }
		});		

	
	});

	
	$("#addTextBtn").click( ()=>{
		let textData = $("#textToAdd").val();

		let data = { textToAdd: textData }
		
		if ( !textData ) return;
		
		alertToast( "Wait... Don't move!", "I will refresh this page when I finish...", icon = 'warning' )
		
		$.ajax ({
		    url: "/rag/add",
		    type: "POST",
		    data: JSON.stringify( {data : data } ),
		    dataType: "json",
		    contentType: "application/json; charset=utf-8",
		    success: function( data ){
				console.log( data );
		    }
		});		


	});
	

})

function putHisText( text ){
	let msg = '<div class="direct-chat-msg">' +
      '<div class="direct-chat-info clearfix">' +
        '<span class="direct-chat-name pull-left">Minerva I.A.</span>' +
      '</div>' +
      '<img class="direct-chat-img" src="common/img/default-logo.jpg" alt="Message User Image">' +
      '<div class="direct-chat-text">' +
        text + 
      '</div>' +
    '</div>';
	$("#msgContainer").append( msg);
}

function putMyText( text ){
	let msg = '<div class="direct-chat-msg right">' +
      '<div class="direct-chat-info clearfix">' +
        '<span class="direct-chat-name pull-right">Você</span>' +
      '</div>' +
      '<img class="direct-chat-img" src="common/img/default-logo.jpg" alt="Message User Image">' +
      '<div class="direct-chat-text">' +
        text +
      '</div>' +
    '</div>';
	$("#msgContainer").append( msg);
}

