import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

public class MainClient {
	
	static private Map<Integer, List<IProduct>> buyMap;
	static private List<IProduct> listPro;
	static private Map<Integer, List<IProduct>> sellMap;
	static private List<IProduct> listProSell;
	static private int idClient;
	static private int loginAttempt = 3;
	
	public static void main(String[] args) throws RemoteException, NotBoundException, UnknownHostException, Exception {
		
		buyMap = new HashMap<Integer, List<IProduct>>();
		listPro = new ArrayList<IProduct>();
		sellMap = new HashMap<Integer, List<IProduct>>();
		listProSell = new ArrayList<IProduct>();
		
		// connection to Employee service
		String ip = Inet4Address.getLocalHost().getHostAddress();
		if (ip == null || ip == "") {
			ip = "localhost";
		}
		Registry r = LocateRegistry.getRegistry(ip, 1708);
		IManageEmployes employeeManager = (IManageEmployes) r.lookup("//" + ip + "/EmployeeService");
		
		// login
		boolean b;
		try {
			b = login(employeeManager);
			if (!b) {
				System.out.println("Sorry! You have reached the maximum of tries");
			} else {
				System.out.println("You successfuly logged in");
				
				// connection to IfShare service
				Registry r2 = LocateRegistry.getRegistry(ip, 1709);
				IIfShare service = (IIfShare) r2.lookup("//" + ip + "/IFShareService");	
				
				// get all the products from IfShare
				List<IProduct> products = service.getAllProduct();
				System.out.println("Here are all the products : ");
				for(IProduct p : products) {
					// TODO to delete
					p.setAvailable(true);
					System.out.println("Id Product: " + p.getId() + " type: " + p.getType() + " name: " + p.getName() + " price: " + p.getPrice() + " availible: " + p.isAvailable());
				}
							
				System.out.println("//////////////////////////////////////");
				// TODO
				// verify bank account
				
				// buy a product
				System.out.println("Place your order");
				placeOrder(service, products);
								
				// Option menu
				optionMenu("buy", service, products);
			    
			    
			    // the client's products he bought
				System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
				for(Entry<Integer, List<IProduct>> e : buyMap.entrySet()) {
					Integer key = e.getKey();
					List<IProduct> lp =e.getValue();
					for(IProduct p : lp)
					{
						System.out.println("key: " + key + " Id Product: " + p.getId() + " type: " + p.getType() + " name: " + p.getName() + " price: " + p.getPrice() + " availible: " + p.isAvailable());
					}
				}
				
				System.out.println("/////////////////////////////");
				sellProduct();
				// Option menu
				optionMenu("sell", service, products);
				
				
				// the client's sold product list
				System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
				for(Entry<Integer, List<IProduct>> e : sellMap.entrySet()) {
					Integer key = e.getKey();
					List<IProduct> lp =e.getValue();
					for(IProduct p : lp)
					{
						System.out.println("idClient: " + key + " Id Product: " + p.getId() + " type: " + p.getType() + " name: " + p.getName() + " price: " + p.getPrice() + " availible: " + p.isAvailable() + " Note: " + p.getNote() + " State: " + p.getState());
					}
				}
				
				// the client's bought product list
				System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
				for(Entry<Integer, List<IProduct>> e : buyMap.entrySet()) {
					Integer key = e.getKey();
					List<IProduct> lp =e.getValue();
					for(IProduct p : lp)
					{
						System.out.println("key: " + key + " Id Product: " + p.getId() + " type: " + p.getType() + " name: " + p.getName() + " price: " + p.getPrice() + " availible: " + p.isAvailable());
					}
				}
			}
			
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
	}
	
	public static boolean login(IManageEmployes employeeManager) throws RemoteException {
		boolean b = false;
		while(loginAttempt !=0 && !b) {
			System.out.println("Please enter your ID : ");
			Scanner scanner = new Scanner(System.in) ;
			idClient = scanner.nextInt();
			System.out.println("Please enter your password : ");
			scanner = new Scanner(System.in);
			String password = scanner.nextLine();
			b = employeeManager.login(idClient, password);
			loginAttempt--;
			if (!b) {
				System.out.println("Wrong id or password, you have " + loginAttempt + " more tries");
			}
		}
		return b;
	}
	
	public static void optionMenu(String action, IIfShare service, List<IProduct> products) throws RemoteException{
		boolean keepGoing = true;
		int response = 0;
		while (keepGoing) {
	    	System.out.println("Please select an option from the menu:");
		    System.out.println("1. Continue");
		    System.out.println("2. Finish");
			Scanner scanner = new Scanner(System.in);
			response = scanner.nextInt();
		    switch (response) {
		    	case 1:
		    		if (action.equals("buy")) {
		    			placeOrder(service, products);
		    		} else {
		    			sellProduct();
		    		}
		    		break;
		        case 2:
		        	keepGoing = false;
		            break;
		        default:
		            System.out.println("Unknown option");
		            break;
		    }
	    }
	}
	
	public static void placeOrder(IIfShare service, List<IProduct> products) throws RemoteException {
		String type, nameProduct = null;
		System.out.println("Please state the type of your order : ");
		Scanner scanner = new Scanner(System.in) ;
		type = scanner.nextLine();
		System.out.println("Please state the name of the product : ");
		scanner = new Scanner(System.in) ;
		nameProduct = scanner.nextLine();
		buyProduct(type, nameProduct, idClient, service, products);
	}
	
	public static void buyProduct(String type, String productName, int idClient, IIfShare service, List<IProduct> products) throws RemoteException {
		
		String result = service.buyProduct(type, productName, idClient);
		boolean b = false;
		
		for(IProduct p : products) {
			if(p.getId().contains(result)) {
				if (buyMap.containsKey(idClient)) {
					buyMap.get(idClient).add(p);
				} else {
					listPro.add(p);
					buyMap.put(idClient, listPro);
				}
				System.out.println("Id Product: " + p.getId() + " type: " + p.getType() + " name: " + p.getName() + " price: " + p.getPrice() + " availible: " + p.isAvailable());
				b = true;
			}
		}
		if(!b) System.out.println(result);
	}
	
	public static void sellProduct() throws RemoteException {
		
		System.out.println("Please select the product id that you want to sell");
		Scanner scSell = new Scanner(System.in);
		String idProduct = scSell.nextLine();
		System.out.println("Give a rating to this product between 0 to 5");
		Scanner scNote = new Scanner(System.in);
		float note = scNote.nextFloat();
		System.out.println("What is the state of this product?");
		Scanner scState = new Scanner(System.in);
		String state = scState.nextLine();
		
		for(Entry<Integer, List<IProduct>> e : buyMap.entrySet()) {
			Integer idClient = e.getKey();
			List<IProduct> lp =e.getValue();
			for(IProduct p : lp) {
				if(p.getId().contains(idProduct)) {
					p.setState(state);
					p.setNote(note);
					p.setAvailable(true);
					if (sellMap.containsKey(idClient)) {
						sellMap.get(idClient).add(p);
					} else {
						listProSell.add(p);
						sellMap.put(idClient, listProSell);
					}
				}
			}
		}
		
		// remove the sold product from the client's list
		// /!\ had to do it like this because I had a problem when removing the product in the for loop before (problem: concurrence)
		for(Entry<Integer, List<IProduct>> e : sellMap.entrySet()) {
			Integer idClient = e.getKey();
			List<IProduct> lp =e.getValue();
			for(IProduct p : lp) {
				buyMap.get(idClient).remove(p);
			}
		}
	}
	}

