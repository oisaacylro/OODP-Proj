package Classes;

import java.util.ArrayList;
import java.util.Scanner;
public class MenuManager {
	
	static ArrayList<MenuItem> menuList = new ArrayList<MenuItem>();
	static ArrayList<PromotionItem> promotionList = new ArrayList<PromotionItem>();
	
	public MenuManager()
	{
		//load Menu from txt file
		//load promotion from txt file
	}
	
	public static float getItemPrice(int itemID)
	{
		for(int i = 0; i< menuList.size();i++)
		{
			if(menuList.get(i).itemID == itemID)
			{
				return menuList.get(i).price;
			}
		}
		return -1;
	}
	public void choices(Scanner s)
	{
		int choice = -1;
		while(choice!= 0)
		{
			//choices
			choice = s.nextInt();
			switch(choice)
			{
			case 1:
				break;
			case 2 :
				break;
			default:
				System.out.println("Please enter a valid choice.");	
			}
		}
	}
	
}
