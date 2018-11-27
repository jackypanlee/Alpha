package xyz.ts.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
/*
 *2018-11-27 17:59:52
 *A demo for checking the xml structure
 * */
public class CheckXMLStructure {
	public static void main(String[] args) {
		try {
			Map<String, String> nsMap = new HashMap<String, String>();
			nsMap.put("xmlns", "http://www.opentravel.org/OTA/2003/05");
			SAXReader saxReader = new SAXReader();
			saxReader.getDocumentFactory().setXPathNamespaceURIs(nsMap);
			String path = "C:/Users/Jacky/Desktop/DefaultHierarchy.xml";
			//path = "C:/Users/Jacky/Desktop/DefaultHierarchy-172.xml";
			Document document = saxReader.read(new File(path));
			System.out.println("--------国家数量--------");
			System.out.println(getLocationsCountByLocationType(document,"country"));
			System.out.println("--------城市数量--------");
			System.out.println(getLocationsCountByLocationType(document,"city"));
			System.out.println("--------机场数量--------");
			System.out.println(getLocationsCountByLocationType(document,"airport"));
			System.out.println("--------经纬度检测--------");
			getAllLatitudeAndLongitude(document);
			System.out.println("--------ServingAirportIsEqualIATA--------");
			checkoutServingAirportIsEqualIATA(document);
			System.out.println("--------检测城市节点完整性--------");
			checkCityTypeLocation(document);
			//打印searchable=true 的location id
			System.out.println("--------searchable检测--------");
			getIdBySearchable(document,"continent");
			getIdBySearchable(document,"country");
			getIdBySearchable(document,"state");
			getIdBySearchable(document,"city");
			getIdBySearchable(document,"airport");
			System.out.println("--------城市类型节点检测--------");
			checkCityTypeLocation(document);
			System.out.println("--------机场类型节点检测--------");
			checkAirPortTypeLocation(document);
			System.out.println("--------父节点性检测--------");
			checkParentLocationsLocationReference(document);
			System.out.println("--------子节点性检测--------");
			checkChildLocationsLocationReference(document);
			//System.out.println("--------层级结构检测(待完善)--------");
			//checkHierarchy(document);
			
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 *统计location节点数量
	 */
	public static int getLocationsCountByLocationType(Document document,String locationType){
		List<Node> locations = document.selectNodes("/xmlns:LocationHierarchy/xmlns:Locations/xmlns:Location[@LocationType='"+locationType+"']");
		/*
		for (Node location : locations) {
			System.out.println(location.valueOf("./xmlns:Codes/xmlns:Code[@Context='IATA']/@Value"));
		}*/
		return locations.size();
	}
	
	/**
	 *获取Searchable属性不正确的项
	 */
	public static void getIdBySearchable(Document document,String locationType){
		@SuppressWarnings("unchecked")
		List<Node> locations = document.selectNodes("/xmlns:LocationHierarchy/xmlns:Locations/xmlns:Location[@LocationType='"+locationType+"']");
		for (Node location : locations) {
			if(StringUtils.equalsIgnoreCase(locationType, "city")){
				String airportId = location.selectSingleNode("./xmlns:ChildLocations/xmlns:LocationReference").valueOf("@Id");
				Node airport = document.selectSingleNode("/xmlns:LocationHierarchy/xmlns:Locations/xmlns:Location[@Id='" + airportId + "']");
				int size = airport.selectNodes("./xmlns:AssociatedLocations/xmlns:LocationReference").size();
				String searchable = location.valueOf("@Searchable");
				if(size>0 && StringUtils.equalsIgnoreCase(searchable, "false")){
					System.out.println("有航线，searchable属性应该为true:"+location.valueOf("@Id"));
				}else if(size ==0 && StringUtils.equalsIgnoreCase(searchable, "true")){
					System.out.println("没有航线，Searchable属性应该为false:"+location.valueOf("@Id"));
				}
			}else{
				if(StringUtils.equalsIgnoreCase(location.valueOf("@Searchable"),"true")){
					//System.out.println(location.valueOf("./xmlns:Codes/xmlns:Code[@Context='IATA']/@Value"));
					//System.out.println(location.valueOf("@Id"));
					System.out.println("非城市节点拥有Searchable=true的节点ID:"+location.valueOf("@Id"));
				}
			}
		}
	}
	
	/**
	 *获取城市和机场的经纬度
	 */
	public static void getAllLatitudeAndLongitude(Document document){
		@SuppressWarnings("unchecked")
		List<Node> airports = document.selectNodes("/xmlns:LocationHierarchy/xmlns:Locations/xmlns:Location[@LocationType='airport']");
		for (Node airport : airports) {

			String city_id = airport.selectSingleNode("./xmlns:ParentLocations/xmlns:LocationReference").valueOf("@Id");
			Node city = document.selectSingleNode("/xmlns:LocationHierarchy/xmlns:Locations/xmlns:Location[@Id='" + city_id + "']");
			
			Double cityLatitude = Double.valueOf(airport.valueOf("./xmlns:Position/@Latitude"));
			Double cityLongitude = Double.valueOf(airport.valueOf("./xmlns:Position/@Longitude"));
			Double airportLatitude = Double.valueOf(city.valueOf("./xmlns:Position/@Latitude"));
			Double airportLongitude = Double.valueOf(city.valueOf("./xmlns:Position/@Longitude"));
			if(Math.abs(cityLatitude-airportLatitude)>0.5 || Math.abs(cityLongitude-airportLongitude)>0.5){
				System.out.println(airport.valueOf("./xmlns:Codes/xmlns:Code[@Context='IATA']/@Value"));
				System.out.println(cityLatitude+":"+airportLatitude);
				System.out.println(cityLongitude+":"+airportLongitude);
			}
		}
	}
	
	/**
	 * 打印城市的servingAirport不等于IATA的节点ID
	 * @param document
	 */
	@SuppressWarnings("unused")
	private static void checkoutServingAirportIsEqualIATA(Document document){
		@SuppressWarnings("unchecked")
		List<Node> locations = document.selectNodes("/xmlns:LocationHierarchy/xmlns:Locations/xmlns:Location[@LocationType='city']");
		for (Node location : locations) {
			@SuppressWarnings("unchecked")
			Node codeServingAirport = location.selectSingleNode("./xmlns:Codes/xmlns:Code[@Context='ServingAirport']");
			Node codeIATAs = location.selectSingleNode("./xmlns:Codes/xmlns:Code[@Context='IATA']");
			String IATA = codeIATAs.valueOf("@Value");
			String ServingAirport = codeServingAirport.valueOf("@Value");
				if(!IATA.equals(ServingAirport)){
					System.out.println(location.valueOf("@Id")+":"+IATA+":"+ServingAirport);
				}
			}
		}
	
	/**
	 * 检测城市节点完整性
	 * @param document
	 */
	private static void checkCityTypeLocation(Document document){
		@SuppressWarnings("unchecked")
		List<Node> locations = document.selectNodes("/xmlns:LocationHierarchy/xmlns:Locations/xmlns:Location[@LocationType='city']");
		for (Node location : locations) {
			@SuppressWarnings("unchecked")
			List<Node> codesIATA = location.selectNodes("./xmlns:Codes/xmlns:Code[@Context='IATA']");
			@SuppressWarnings("unchecked")
			List<Node> codesServingAirport = location.selectNodes("./xmlns:Codes/xmlns:Code[@Context='ServingAirport']");
			if(codesIATA.size()<1||codesServingAirport.size()<1){
				System.out.println("缺少IATA或者ServingAirport：" + location.valueOf("@Id"));
			}else if(codesIATA.size()>1||codesServingAirport.size()>1){
				System.out.println("多個IATA或者ServingAirport：" + location.valueOf("@Id"));
			}else if(!StringUtils.equals(codesIATA.get(0).valueOf("@Value"), codesServingAirport.get(0).valueOf("@Value"))){
				System.out.println("IATA不等於ServingAirport：" + location.valueOf("@Id"));
			}
		}
	}
	
	
	/**
	 * 检测机场节点完整性
	 * @param document
	 */
	private static void checkAirPortTypeLocation(Document document){
		@SuppressWarnings("unchecked")
		List<Node> locations = document.selectNodes("/xmlns:LocationHierarchy/xmlns:Locations/xmlns:Location[@LocationType='airport']");
		for (Node location : locations) {
			@SuppressWarnings("unchecked")
			List<Node> codesIATA = location.selectNodes("./xmlns:Codes/xmlns:Code[@Context='IATA']");
			List<Node> codesServingAirport = location.selectNodes("./xmlns:Codes/xmlns:Code[@Context='ServingAirport']");
			if(codesIATA.size()<1){
				System.out.println("IATA缺失：" + location.valueOf("@Id"));
			}else if(codesIATA.size()>1||codesServingAirport.size()>0){
				System.out.println("多余IATA或者ServingAirport：" + location.valueOf("@Id"));
			}
		}
	}
	
	
	/**
	 * 父节点唯一性检测
	 * @param document
	 */
	private static void checkParentLocationsLocationReference(Document document){
		@SuppressWarnings("unchecked")
		List<Node> locations = document.selectNodes("/xmlns:LocationHierarchy/xmlns:Locations/xmlns:Location");
		//List<Node> locations = document.selectNodes("/xmlns:LocationHierarchy/xmlns:Locations/xmlns:Location[@LocationType='"+locationType+"']");
		
		for (Node location : locations) {
			@SuppressWarnings("unchecked")
			List<Node> parentLocationsReference = location.selectNodes("./xmlns:ParentLocations/xmlns:LocationReference");
			if(StringUtils.equals(location.valueOf("@LocationType"),"continent")){continue;}
			if(parentLocationsReference.size()==0){
				System.out.println("父节点缺失：" + location.valueOf("@Id"));
			}else if(parentLocationsReference.size()>1){
				System.out.println("父节点不唯一：" + location.valueOf("@Id"));
			}else{
				String parentLocationId = parentLocationsReference.get(0).valueOf("@Id");
				@SuppressWarnings("unchecked")
				List<Node> parentLocations = document.selectNodes("/xmlns:LocationHierarchy/xmlns:Locations/xmlns:Location[@Id='"+parentLocationId+"']");
				if(parentLocations.size()==0){
					System.out.println("父节点指向为空");
				}
			}
		}
	}
	
	/**
	 * 子节点检测
	 * @param document
	 */
	private static void checkChildLocationsLocationReference(Document document){
		@SuppressWarnings("unchecked")
		List<Node> locations = document.selectNodes("/xmlns:LocationHierarchy/xmlns:Locations/xmlns:Location");
		//List<Node> locations = document.selectNodes("/xmlns:LocationHierarchy/xmlns:Locations/xmlns:Location[@LocationType='"+locationType+"']");
		
		for (Node location : locations) {
			@SuppressWarnings("unchecked")
			List<Node> childLocationsReference = location.selectNodes("./xmlns:ChildLocations/xmlns:LocationReference");
			if(!StringUtils.equals(location.valueOf("@LocationType"),"city")){continue;}
			if(childLocationsReference.size()==0){
				System.out.println("子节点缺失：" + location.valueOf("@Id"));
			}else if(childLocationsReference.size()>1){
				System.out.println("子节点不唯一：" + location.valueOf("@Id"));
			}else{
				String childLocationId = childLocationsReference.get(0).valueOf("@Id");
				@SuppressWarnings("unchecked")
				List<Node> childLocations = document.selectNodes("/xmlns:LocationHierarchy/xmlns:Locations/xmlns:Location[@Id='"+childLocationId+"']");
				if(childLocations.size()==0){
					System.out.println("子节点指向为空");
				}
			}
		}
	}
	
	/**
	 * 层级结构完整性
	 */
	private static void checkHierarchy(Document document){
		@SuppressWarnings("unchecked")
		List<Node> continentLocations = document.selectNodes("/LocationHierarchy/Locations/Location[@LocationType='continent']");
		for (Node continentLocation : continentLocations) {
			@SuppressWarnings("unchecked")
			List<Node> childLocations = continentLocation.selectNodes("./ChildLocations/LocationReference/@Id']");
		}
	}
	
	
	
	
	/**
	 * --检测拥有多个ServingAirport的节点（城市）;
	 * --并可以打印具体哪个城市节点多了哪个ServingAirport节点的
	 * 城市的ServingAirport Code 和 城市子节点的机场IATA不一致
	 * @param document
	 */
	private static void checkoutMultipleServingAirportX(Document document){
		//List<Node> locations = document.selectNodes("//xmlns:Location[@LocationType='city']");
		@SuppressWarnings("unchecked")
		List<Node> locations = document.selectNodes("/LocationHierarchy/Locations/Location[@LocationType='city']");
		//List<Node> locations = document.selectNodes("/xmlns:LocationHierarchy/xmlns:Locations/xmlns:Location[@LocationType='city']");
		for (Node location : locations) {

			List<String> childAirportLocationIATACodeList = null;
			childAirportLocationIATACodeList = new ArrayList<String>();
			//获取location的ChildLocations的IATA,并暂存到childAirportLocationIATACodeList集合中
			@SuppressWarnings("unchecked")
			List<Node> LocationReferences = location.selectNodes("./ChildLocations/LocationReference");
			for (Node LocationReference : LocationReferences) {
				Node childAirportLocation = document.selectSingleNode("/LocationHierarchy/Locations/Location[@Id='"+LocationReference.valueOf("@Id")+"']");
				childAirportLocationIATACodeList.add(childAirportLocation.valueOf("./Codes/Code[@Context='IATA']/@Value"));
			}
			
			//获取location 节点中拥有Context='ServingAirport'属性的Code子节点集，遍历和childAirportLocationIATACodeList集合比较
			@SuppressWarnings("unchecked")
			List<Node> codes = location.selectNodes("./Codes/Code[@Context='ServingAirport']");
			
			
			for (Node code : codes) {
				//如果ServingAirport的Code值不存在于ChildLocations中的IATA
				if(!childAirportLocationIATACodeList.contains(code.valueOf("@Value"))){
					System.out.print(location.valueOf("@Id"));
					//System.out.print(code.valueOf("@Value") + " ");
				}
			}
			System.out.println(); 
		}
	}
	/**
	 *	检测拥有多个IATA的节点（城市&机场）
	 * @param document
	 */
	private static void checkoutMultipleIATA(Document document){
		//List<Node> locations = document.selectNodes("//xmlns:Location[@LocationType='city']");
		@SuppressWarnings("unchecked")
		//List<Node> locations = document.selectNodes("/xmlns:LocationHierarchy/xmlns:Locations/xmlns:Location[@LocationType='city']");
		List<Node> locations = document.selectNodes("/LocationHierarchy/Locations/Location[@LocationType='city' or @LocationType='airport']");
		for (Node location : locations) {
			@SuppressWarnings("unchecked")
			//List<Node> codes = location.selectNodes("./Codes/Code[@Context='IATA']");
			List<Node> codes = location.selectNodes("./Codes/Code[@Context='IATA']");
			//List<Node> codes = location.selectNodes("./xmlns:Codes/xmlns:Code[@Context='IATA']");
			if(codes.size()>1){
				System.out.print(location.valueOf("@Id")+" ");
				for (Node code : codes) {
					System.out.print(code.valueOf("@Value")+" ");
				}
				System.out.println();
			}
		}
	}
	
	/**
	 * 检测拥有ServingAirport的（机场）
	 * @param document
	 */
	private static void checkoutAirportServingAirport(Document document){
		//List<Node> locations = document.selectNodes("//xmlns:Location[@LocationType='city']");
		@SuppressWarnings("unchecked")
		//List<Node> locations = document.selectNodes("/xmlns:LocationHierarchy/xmlns:Locations/xmlns:Location[@LocationType='city']");
		List<Node> locations = document.selectNodes("/LocationHierarchy/Locations/Location[@LocationType='airport']");
		for (Node location : locations) {
			@SuppressWarnings("unchecked")
			List<Node> codes = location.selectNodes("./Codes/Code[@Context='ServingAirport']");
			//List<Node> codes = location.selectNodes("./xmlns:Codes/xmlns:Code[@Context='IATA']");
			if(codes.size()>0){
				//System.out.println(location.asXML());
				System.out.print(location.valueOf("@Id")+" ");
				for (Node code : codes) {
					System.out.print(code.valueOf("@Value")+" ");
				}
				System.out.println();
			}
		}
	}
	
	
	
	/**
	 * 多个父节点 
	 */
	private static void checkLocationReference(Document document){
		@SuppressWarnings("unchecked")
		//List<Node> locations = document.selectNodes("/LocationHierarchy/Locations/Location[@LocationType='airport']");
		List<Node> locations = document.selectNodes("/LocationHierarchy/Locations/Location[@LocationType='city' or @LocationType='airport' or @LocationType='state' or @LocationType='country']");
		for (Node location : locations) {
			@SuppressWarnings("unchecked")
			List<Node> ParentLocations = location.selectNodes("./ParentLocations/LocationReference/@Value']");
			if(ParentLocations.size()>1){
				System.out.println(location.valueOf("@Id"));
			}
		}
	}
	
	
	private static boolean isExist(Document document,String Id){
		@SuppressWarnings("unchecked")
		List<Node> locations = document.selectNodes("/LocationHierarchy/Locations/Location[@Id='"+Id+"']");
		if(locations.size()>1){
			System.out.println("该节点存在多个：" + Id);
			return false;
		}else if(locations.size()<0){
			System.out.println("该节点不存在：" + Id);
			return false;
		}else{
			return true;
		}
	}
	
	private static boolean hasChildLocationsNodes(Document document,String Id){
		if(!StringUtils.equals(document.valueOf("/LocationHierarchy/Locations/Location/@LocationType"),"airport")){
			List<Node> continentLocations = document.selectNodes("/LocationHierarchy/Locations/Location[@Id='"+Id+"']");
			return hasChildLocationsNodes(document,Id);
		}
		return false;
	}
}
