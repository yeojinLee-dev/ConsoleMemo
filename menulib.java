package lib;

import java.util.ArrayList;
import java.util.List;

public class menulib {
    class MenuItem {
        int MenuNo;
        String Title;
    

        public MenuItem(int No, String Title) {
            this.MenuNo = No;
            this.Title = Title;
        }
    }

    class MenuList {
        List<MenuItem> list = new ArrayList<>();

        public void add(int No, String Title) {
            MenuItem menu = new MenuItem(No, Title);
            list.add(menu);
        }

        public void showMenuList() {
            for (int i = 0; i < list.size(); i++) {
                MenuItem menu = list.get(i);
                System.out.printf("%d. %s\n", menu.MenuNo, menu.Title);
            }
            System.out.println();
        }
    }
}