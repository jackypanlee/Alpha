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
			System.out.println("--------��������--------");
			System.out.println(getLocationsCountByLocationType(document,"country"));
			System.out.println("--------��������--------");
			System.out.println(getLocationsCountByLocationType(document,"city"));
			System.out.println("--------��������--------");
			System.out.println(getLocationsCountByLocationType(document,"airport"));
			System.out.println("--------��γ�ȼ��--------");
			getAllLatitudeAndLongitude(document);
			System.out.println("--------ServingAirportIsEqualIATA--------");
			checkoutServingAirportIsEqualIATA(document);
			System.out.println("--------�����нڵ�������--------");
			checkCityTypeLocation(document);
			//��ӡsearchable=true ��location id
			System.out.println("--------searchable���--------");
			getIdBySearchable(document,"continent");
			getIdBySearchable(document,"country");
			getIdBySearchable(document,"state");
			getIdBySearchable(document,"city");
			getIdBySearchable(document,"airport");
			System.out.println("--------�������ͽڵ���--------");
			checkCityTypeLocation(document);
			System.out.println("--------�������ͽڵ���--------");
			checkAirPortTypeLocation(document);
			System.out.println("--------���ڵ��Լ��--------");
			checkParentLocationsLocationReference(document);
			System.out.println("--------�ӽڵ��Լ��--------");
			checkChildLocationsLocationReference(document);
			//System.out.println("--------�㼶�ṹ���(������)--------");
			//checkHierarchy(document);
			
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 *ͳ��location�ڵ�����
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
	 *��ȡSearchable���Բ���ȷ����
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
					System.out.println("�к��ߣ�searchable����Ӧ��Ϊtrue:"+location.valueOf("@Id"));
				}else if(size ==0 && StringUtils.equalsIgnoreCase(searchable, "true")){
					System.out.println("û�к��ߣ�Searchable����Ӧ��Ϊfalse:"+location.valueOf("@Id"));
				}
			}else{
				if(StringUtils.equalsIgnoreCase(location.valueOf("@Searchable"),"true")){
					//System.out.println(location.valueOf("./xmlns:Codes/xmlns:Code[@Context='IATA']/@Value"));
					//System.out.println(location.valueOf("@Id"));
					System.out.println("�ǳ��нڵ�ӵ��Searchable=true�Ľڵ�ID:"+location.valueOf("@Id"));
				}
			}
		}
	}
	
	/**
	 *��ȡ���кͻ����ľ�γ��
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
	 * ��ӡ���е�servingAirport������IATA�Ľڵ�ID
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
	 * �����нڵ�������
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
				System.out.println("ȱ��IATA����ServingAirport��" + location.valueOf("@Id"));
			}else if(codesIATA.size()>1||codesServingAirport.size()>1){
				System.out.println("����IATA����ServingAirport��" + location.valueOf("@Id"));
			}else if(!StringUtils.equals(codesIATA.get(0).valueOf("@Value"), codesServingAirport.get(0).valueOf("@Value"))){
				System.out.println("IATA�����ServingAirport��" + location.valueOf("@Id"));
			}
		}
	}
	
	
	/**
	 * �������ڵ�������
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
				System.out.println("IATAȱʧ��" + location.valueOf("@Id"));
			}else if(codesIATA.size()>1||codesServingAirport.size()>0){
				System.out.println("����IATA����ServingAirport��" + location.valueOf("@Id"));
			}
		}
	}
	
	
	/**
	 * ���ڵ�Ψһ�Լ��
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
				System.out.println("���ڵ�ȱʧ��" + location.valueOf("@Id"));
			}else if(parentLocationsReference.size()>1){
				System.out.println("���ڵ㲻Ψһ��" + location.valueOf("@Id"));
			}else{
				String parentLocationId = parentLocationsReference.get(0).valueOf("@Id");
				@SuppressWarnings("unchecked")
				List<Node> parentLocations = document.selectNodes("/xmlns:LocationHierarchy/xmlns:Locations/xmlns:Location[@Id='"+parentLocationId+"']");
				if(parentLocations.size()==0){
					System.out.println("���ڵ�ָ��Ϊ��");
				}
			}
		}
	}
	
	/**
	 * �ӽڵ���
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
				System.out.println("�ӽڵ�ȱʧ��" + location.valueOf("@Id"));
			}else if(childLocationsReference.size()>1){
				System.out.println("�ӽڵ㲻Ψһ��" + location.valueOf("@Id"));
			}else{
				String childLocationId = childLocationsReference.get(0).valueOf("@Id");
				@SuppressWarnings("unchecked")
				List<Node> childLocations = document.selectNodes("/xmlns:LocationHierarchy/xmlns:Locations/xmlns:Location[@Id='"+childLocationId+"']");
				if(childLocations.size()==0){
					System.out.println("�ӽڵ�ָ��Ϊ��");
				}
			}
		}
	}
	
	/**
	 * �㼶�ṹ������
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
	 * --���ӵ�ж��ServingAirport�Ľڵ㣨���У�;
	 * --�����Դ�ӡ�����ĸ����нڵ�����ĸ�ServingAirport�ڵ��
	 * ���е�ServingAirport Code �� �����ӽڵ�Ļ���IATA��һ��
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
			//��ȡlocation��ChildLocations��IATA,���ݴ浽childAirportLocationIATACodeList������
			@SuppressWarnings("unchecked")
			List<Node> LocationReferences = location.selectNodes("./ChildLocations/LocationReference");
			for (Node LocationReference : LocationReferences) {
				Node childAirportLocation = document.selectSingleNode("/LocationHierarchy/Locations/Location[@Id='"+LocationReference.valueOf("@Id")+"']");
				childAirportLocationIATACodeList.add(childAirportLocation.valueOf("./Codes/Code[@Context='IATA']/@Value"));
			}
			
			//��ȡlocation �ڵ���ӵ��Context='ServingAirport'���Ե�Code�ӽڵ㼯��������childAirportLocationIATACodeList���ϱȽ�
			@SuppressWarnings("unchecked")
			List<Node> codes = location.selectNodes("./Codes/Code[@Context='ServingAirport']");
			
			
			for (Node code : codes) {
				//���ServingAirport��Codeֵ��������ChildLocations�е�IATA
				if(!childAirportLocationIATACodeList.contains(code.valueOf("@Value"))){
					System.out.print(location.valueOf("@Id"));
					//System.out.print(code.valueOf("@Value") + " ");
				}
			}
			System.out.println(); 
		}
	}
	/**
	 *	���ӵ�ж��IATA�Ľڵ㣨����&������
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
	 * ���ӵ��ServingAirport�ģ�������
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
	 * ������ڵ� 
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
			System.out.println("�ýڵ���ڶ����" + Id);
			return false;
		}else if(locations.size()<0){
			System.out.println("�ýڵ㲻���ڣ�" + Id);
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
