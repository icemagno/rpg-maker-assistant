package br.com.j1scorpii.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import jakarta.annotation.PostConstruct;
import nortantis.MapCreator;
import nortantis.MapSettings;
import nortantis.platform.Image;
import nortantis.platform.PlatformFactory;
import nortantis.platform.awt.AwtFactory;
import nortantis.util.Assets;
import nortantis.util.FileHelper;
import nortantis.util.ImageHelper;
import nortantis.util.Logger;
import nortantis.SettingsGenerator;
import nortantis.Stroke;
import nortantis.StrokeType;

import org.springframework.core.io.Resource;

@Service
public class NovoTesteService {

	@Autowired private OllamaEmbeddingModel em;	
	
	private final ChatClient cc;
	private final String textModel = "HammerAI/mythomax-l2:latest";
	private final double textTemperature = 0.8;

	private final double imageTemperature = 0.1;
	private final String imageModel = "gemma3:27b";  //"llava-phi3:latest";

	private final String baseFolderData = "/nortantis";
	private final String generatedMapsFolderPath = baseFolderData + "/generated_maps";
	private final String customArtPackFolderPath = baseFolderData + "/packs/";
	private final String mapFilesFolderPath = baseFolderData + "/map_configs";
	private final String assetsFolderPath = baseFolderData + "/assets";
	private final String artPack = "bonus_art";


	@PostConstruct
	private void init() {
		/*               NORTANTIS SETTINGS                   */
		PlatformFactory.setInstance( new AwtFactory() );
		Assets.setAssetsPath( assetsFolderPath );
		FileHelper.createFolder( generatedMapsFolderPath );
		FileHelper.createFolder( mapFilesFolderPath );
		/* -------------------------------------------------- */
		long rand = new Random().nextLong();

		try {
			String uuid = UUID.randomUUID().toString().replaceAll("-", "").substring(0,8);
			generateRandomMap( rand , "aa_" + uuid );
			//testMap();
		} catch (Exception e) {
			e.printStackTrace();
		}
		

		/*
 		JSONArray objects = new JSONArray();
		JSONArray monsters  = new JSONArray();
		String dungeonFromImage = describeDungeonFromImage( "C:/Magno/Projetos/minerva2/wizard.png", objects, monsters,
			"");	
		System.out.println(  dungeonFromImage );	
		*/

		/*
		JSONObject player1 = generatePlayer("O personagem deverá ser um elfo da floresta. Seu nome é Lúthien.");
		String p1Name = player1.getString("name");
		System.out.println( "-----> PLAYER 1: " + p1Name );

		JSONObject player2 = generatePlayer("O personagem deverá ser um guerreiro humano. Seu nome é Aric.");
		String p2Name = player2.getString("name");
		System.out.println( "-----> PLAYER 2: " + p2Name );

		JSONArray players = new JSONArray();
		players.put( player1 );
		players.put( player2 );

		String dungeon = decribeDungeon( players, objects, monsters, "O cenário é ambientado em uma floresta. Um odor de fumaça está no ar. Está anoitecendo e a luz do sol mal penetra pelas copas das árvores."
		+ " O jogador " + p1Name + " está ferido.");

		System.out.println(  dungeon );
		*/
	}

	private void generateRandomMap( long seed, String expectedFileName ) throws Exception {
		// Default art pack = Assets.installedArtPack
		// Assets.customArtPack
		MapSettings settings = SettingsGenerator.generate( new Random( seed ), "custom" , customArtPackFolderPath + artPack );
		settings.resolution = 1.0;
		settings.drawRoads = true;
		settings.generatedHeight = 4210;
		settings.generatedWidth = 4210;

		settings.hillScale = 0.5;
		settings.mountainScale = 1.0;
		settings.treeHeightScale = 1.0;
		settings.cityScale = 1.0;
		settings.duneScale = 0.8;

		settings.frayedBorder = false;

		settings.drawRegionBoundaries = true;
		settings.drawRegionColors = true;
		settings.regionBoundaryStyle = new Stroke( StrokeType.Dashes, 2.7f);
		settings.roadStyle = new Stroke( StrokeType.Solid, 3.5f);
		settings.regionsRandomSeed = new Random(seed).nextLong();

		Set<String> books = Set.of( "Aventura" );
		settings.books = books;

		settings.cityIconTypeName = "perspective european";

		// Modifica a geografia
		settings.cityProbability = 0.9;				// 0.1 = Mais // 0.9 = Menos
		settings.edgeLandToWaterProbability = 0.1;
		settings.centerLandToWaterProbability = 0.99;
		settings.worldSize = 90000;				// Default = 20.000
		// ------------------------------------------------------------

		MapCreator mapCreator = new MapCreator();
		Logger.println("Creating random map... ");
		Image actual = mapCreator.createMap(settings, null, null);

		String outputImage = mountFullFileName(generatedMapsFolderPath, expectedFileName, "png");
		Logger.println("Writing generated map to '" + outputImage + "'");
		ImageHelper.write(actual, outputImage);

		String outputConfig = mountFullFileName(mapFilesFolderPath, expectedFileName, "nort"); 
		Logger.println("Writing config map file to '" + outputConfig + "'");
		settings.writeToFile( outputConfig );

	}

	private String mountFullFileName( String baseFolder, String fileName, String extension ) {
		String fileFullPath = baseFolder + "/" + FilenameUtils.getBaseName(fileName) + "." + extension;
		return fileFullPath;
	}

	/*
	private void testMap() throws Exception {
		PlatformFactory.setInstance(new AwtFactory());
		Assets.disableAddedArtPacksForUnitTests();
		String[] mapSettingsFileNames = new File(mapSettingsFolderPath).list();
		for (String settingsFileName : mapSettingsFileNames) {
			String expectedMapFilePath = getExpectedMapFilePath(settingsFileName);
			String filePath = mapSettingsFolderPath + "/" + settingsFileName;
			if (!new File(filePath).isDirectory() && !new File(expectedMapFilePath).exists()){
				MapSettings settings = new MapSettings(filePath);
				settings.customImagesPath = mapSettingsFolderPath + "/bonus_art";
				MapCreator mapCreator = new MapCreator();
				Logger.println("Creating map '" + expectedMapFilePath + "'");
				Image map = mapCreator.createMap(settings, null, null);
				ImageHelper.write(map, expectedMapFilePath);
			}
		}
	}
	*/	

    public NovoTesteService(ChatClient.Builder builder) {
        this.cc = builder
			.defaultSystem("You are a Game Master of a RPG game. "
					+ "You create immersive and engaging storylines for players to explore. "
					+ "You describe vivid settings, develop intriguing characters, and craft challenging quests. "
					+ "Your goal is to provide an unforgettable role-playing experience. "
					+ "Answer only in Brazilian Portuguese except for data structures names on JSON objects.")
			.build();
    }	

	private String describeImage( String imagePath ) {
		String res = null;
		try {
			String mime = Files.probeContentType( Paths.get(imagePath) );
			Resource imageResource = new FileSystemResource(imagePath);
			Media imageMedia = new Media(MimeTypeUtils.parseMimeType(mime), imageResource);

			OllamaOptions options = OllamaOptions.builder()
					.model(imageModel)
					.temperature(imageTemperature)
					.build();

			UserMessage userMessage = UserMessage.builder()
					.text("Descreva com objetividade a imagem. Responda em português brasileiro.")
					.media(imageMedia)
					.build();

			String response = cc.prompt()
					.messages( List.of( userMessage ) )
					.options( options )
					.call()
					.content()
					.toString();
			res = response;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	private String describeDungeonFromImage( String imagePath, JSONArray objects, JSONArray monsters, String detalhesAdicionais ){
		System.out.println("Generating dungeon from image " + imagePath + "...");
		String imageDescription = describeImage( imagePath );

		try {
			imageDescription = describeImage( imagePath );
		} catch (Exception e) { }
		
		if( imageDescription == null ) {
			System.out.println( "Failed to describe image." );
		} else {
			String res = callOllama("Utilize a descrição do um cenário a seguir, os objetos e monstros " 
			+ "para elaborar um cenário de RPG mais coerente e detalhado. Use todos os dados fornecidos "
			+ "para criar detalhadamente um cenário que sirva como pano de fundo para um jogo de RPG. O cenário é esse: " + imageDescription
			+ " Opcionalmente, caso hajam, considere os seguintes objetos: " + objects.toString()
			+ " e os seguintes monstros: " + monsters.toString() + ". " 
			+ " Detalhes adicionais: " + detalhesAdicionais );

			return normalizeDungeon( res );
		}
		return null;
	}

	private String decribeDungeon( JSONArray objects, JSONArray monsters, String detalhesAdicionais ){
		System.out.println("Generating dungeon...");
		return normalizeDungeon ( callOllama("Descreva detalhadamente um cenário de RPG. " + detalhesAdicionais
		+ " Caso o cenário ofereça algum perigo, descreva o perigo e informe quais jogadores podem ser afetados e de que forma."
		+ " Seja criativo e coerente na descrição do cenário."
		+ "" ) );
	}

	private String normalizeDungeon( String dungeonDescription ){
		System.out.println("Normalizing dungeon data...");
		if (dungeonDescription == null ) return null;
		String dungeonDataString = callOllama("Como Mestre de Jogo de RPG experiente, verifique a descrição "
		 + "do cenário a seguir e procure alguma inconsistência e pontos que podem ser melhorados para incrementar a dinâmica do jogo. "
		 + "Melhore a disposição de objetos e ameaças (caso haja) no cenário "
		 + "Não modifique a essência do cenário nem os personagens, aprimore principalmente a consistência da descrição. "
		 + "A descrição do cenário é essa: " + dungeonDescription );
		return dungeonDataString;
	}

	private JSONObject generatePlayer( String detalhesAdicionais ){
		System.out.println("Generating player...");
		while( true ) {
			String pp = callOllama("Descreva em brevemente um personagem para um jogo de RPG." + detalhesAdicionais
				+ " Inclua, caso não seja fornecido, o nome, classe, raça, algumas habilidades próprias da raça, poucos equipamentos básicos e uma breve história de fundo."
				+ " Descreva, caso não seja informado, a roupa que o personagem está vestindo e o grau de proteção que ela oferece, em um nível entre 1 e 10."
				+ " Responda em no máximo 150 palavras."
				+ " O nome do personagem deve ser coerente com o sexo e possuir grafia coerente com a raça do personagem."
				+ " A classe deve ser uma entre as seguintes: guerreiro, mago, arqueiro, bárbaro ou vampiro."
				+ " A raça deverá ser coerente com a classe."
				+ " A história de fundo deve ser curta, mas interessante e coerente com a classe e raça do personagem."
				+ " A história de fundo do personagem não deve conter informações sobre roupas ou equipamentos. Apenas sobre sua vida pregressa."
				+ " Roupas e demais acessórios corporais não são considerados equipamentos e não devem constar na lista de equipamentos."
				+ " Os equipamentos e habilidades devem ser coerentes com a classe e raça do personagem.");

			JSONObject playerData = normalizePlayerData( pp );
			if( isPlayerValid( playerData ) ) {
				System.out.println( playerData.toString() );
				return playerData;
			} else {
				System.out.println("Player data invalid, regenerating...");
			}
		}
	}

	private boolean isPlayerValid( JSONObject playerData ){
		List<String> requiredAttributes = List.of("name", "clothes", "class", "kind", "abilities", "equipments", "backstory");
		for( String attr : requiredAttributes ) {
			if( !playerData.has( attr ) ) {
				return false;
			}
		}
		if( !playerData.getJSONObject( "clothes" ).has("description") || !playerData.getJSONObject( "clothes" ).has("protection_level") ) {
			return false;
		}
		return true;
	}	

	private JSONObject normalizePlayerData( String playerDescription ){
		System.out.println("Normalizing player data...");
		if (playerDescription == null ) return null;
		String userDataString = callOllama("Gere uma estrutura JSON a partir do texto a seguir. A estrutura precisa ter os "
		+ "seguintes atributos que você deverá retirar da descrição: 'name', 'clothes' (objeto json com 'description' e 'protection_level'), 'class', 'kind', 'abilities' (array de string), 'equipments' (array de string) e 'backstory'. "
		+ "Garanta que todos os atributos estejam preenchidos com informações coerentes extraídas do texto."
		+ "Retorne apenas o objeto JSON. O texto é esse: " + playerDescription );
		if( userDataString == null ) return null;
		return new JSONObject( userDataString );
	}

	private List<Message> generateMessage(){
		List<Message> ms = new ArrayList<Message>();
		ms.add( new UserMessage("Olá, quem é você?") );
		ms.add( new SystemMessage("Você é um assistente útil.") );
		return ms;
	}

	private String callOllama( String userQuestion ){

		OllamaOptions options = OllamaOptions.builder()
				.model(textModel)
				.temperature( textTemperature )
				.build();

		String answer = null;
		// List<Message> msgs = generateMessage();
        try {
            String response = cc.prompt()
				.user(userQuestion)
				// .messages( msgs )
				.call()
				.content()
				.toString();
			answer = response;
        } catch (Exception e) {
			e.printStackTrace();
        }
		return answer;
	}

	private void t1() {
		float[] embedding =  em.embed("Alow Mundo!");
		System.out.println( "  ---->>>> " + embedding[0] );
	}

}
