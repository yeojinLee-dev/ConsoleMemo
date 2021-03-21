package app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.io.Console;
import java.util.Scanner;

class Memo {
    public static void main(String[] args) {
        DataInput input = new DataInput();
        DataFile data = new DataFile("C:\\Users\\이여진\\Documents\\memodata\\Memo.dat");
        MenuList MemoMenuList = new MenuList();
        MenuList AccountMenuList = new MenuList();
        boolean checkInput = false;
        MemoAccount account = new MemoAccount("C:\\Users\\이여진\\Documents\\memodata\\Account.dat");
        
        MemoMenuList.add(0, "exit");
        MemoMenuList.add(1, "new");
        MemoMenuList.add(2, "show");
        MemoMenuList.add(3, "delete");
        MemoMenuList.add(4, "edit");
        MemoMenuList.add(5, "Account Setting");

        AccountMenuList.add(0, "나가기");
        AccountMenuList.add(1, "계정 새로 만들기");
        AccountMenuList.add(2, "개인정보 변경");
        AccountMenuList.add(3, "비밀번호 변경(또는 찾기)");

        try {
            int menuNo = 999;
            int memoNo;
            MemoItem memo = new MemoItem();
            int LogInNum = 0;

            do {
                account.inputLogIn();
                LogInNum = account.excuteLogIn();
                if (LogInNum == -1)
                    return;
                else if (LogInNum == 0)
                    continue;
                else if (LogInNum == 1)
                    break;
            } while (true);

            do {
                System.out.println("----- 메모장 -----\n");
                menuNo = input.getMenuNo(MemoMenuList);
                switch (menuNo) {
                    case 0:
                        System.out.println(">> 프로그램을 종료합니다.\n");
                        break;

                    case 1:
                        if (input.getMemoData(memo))
                            checkInput = data.appendMemo(memo);
                        if (checkInput == true)
                            System.out.printf("\n>> 정상적으로 메모가 저장되었습니다.\n");
                        break;
                    
                    case 2:
                        if (data.showMemoList() == -1)
                            break;
                        memoNo = data.selectMemo("조회");
                        data.showMemo(memoNo);
                        System.out.println("\n>> 조회 완료");
                        break;
                    
                    case 3:
                        if (data.showMemoList() == -1)
                                break;
                        memoNo = data.selectMemo("삭제");
                        if (memoNo == -1)
                            break;
                        data.deleteMemo(memoNo);
                        break;

                    case 4:
                        if (data.showMemoList() == -1)
                            break;
                        memoNo = data.selectMemo("편집");
                        data.editMemo(memoNo);
                        break;
                    case 5:
                        account.findPassword();
                        break;

                    default:
                        System.out.println(">> 잘못된 메뉴 번호입니다.\n");
                        break;
                }
                System.out.println();

            } while (menuNo != 0);
        } catch (Exception e) {
            System.out.printf("\n>> 입력 오류 발생 : %s\n", e.getMessage());
        } 
    }
}

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

class MemoItem {
    public char isDeleted;
    public String Title;
    public String Content;
}

class DataInput {
    private BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    public int getMenuNo(MenuList menuList) throws Exception {
        try {
            menuList.showMenuList();

            System.out.printf("메뉴 번호를 입력하세요 > ");
            String cmdLine = br.readLine();
            System.out.println();

            return Integer.valueOf(cmdLine);
        } catch (Exception e) {
            return -1;   
        }
    }

    public boolean getMemoData(MemoItem memo) throws Exception {
        System.out.println("[제목을 입력하세요]");
        String title = br.readLine();
        if (title.isEmpty()) {
            System.out.println(">> 입력 취소됨");
            return false;
        }
        System.out.println("\n[내용을 입력하세요]");

        String content = "";
        int ch;
        boolean bDone = false;
        do {
            ch = br.read();
            if (ch == 26 || ch == -1)
                bDone = true;
            else 
                content += (char) ch;
        } while (!bDone);

        memo.Title = title;
        memo.Content = content;

        return true;
    }
}

class DataFile {
    final int HeaderSize = 100;
    final int TitleSize = 100;
    final int ContentSize = 200;
    final int DeleteMarkSize =  Character.BYTES;
    final int DataSize = HeaderSize + TitleSize + ContentSize + DeleteMarkSize;
    String datafile;

    class Header {
        int RecordCount;
        int DeleteCount;
        byte[] Dummy;

        public Header() {
            Dummy = new byte[HeaderSize - (Integer.BYTES*2)];
        }
    }

    public DataFile(String filename) {
        RandomAccessFile file;
        boolean bErr = false;   
        try {
            file = new RandomAccessFile(filename, "r");
            if (file.length() < this.HeaderSize)  
                bErr = true;
            file.close();
            this.datafile = filename;
        } catch (Exception e) {
            bErr = true;
        }

        if (bErr) {
            bErr = false;
            try {
                file = new RandomAccessFile (filename, "rw");
                try {
                    Header header = new Header();
                    header.RecordCount = 0;
                    writeHeader(file, header);
                } finally {
                    file.close();
                }

                this.datafile = filename;
            } catch (Exception e) {
                bErr = true;
            }
        }
    }

    private void writeHeader(RandomAccessFile file, Header header) {
        try {
            file.seek(0);
            file.writeInt(header.RecordCount);
            file.writeInt(header.DeleteCount);
            file.write(header.Dummy);
        } catch (Exception e) {
            System.out.printf("\n>> 데이터 파일 헤더 저장 중 오류 발생 : %s\n", e.getMessage());
        }
    }

    private boolean readHeader(RandomAccessFile file, Header header) {
        try {
            file.seek(0);
            header.RecordCount = file.readInt();
            header.DeleteCount = file.readInt();
            file.read(header.Dummy);

            return true;
        } catch (Exception e) {
            System.out.printf("\n>> 데이터 파일 헤더 읽는 중 오류 발생 : %s\n", e.getMessage());
            return false;
        }
    }

    public boolean appendMemo(MemoItem memo) {
        byte[] buf = new byte[TitleSize + ContentSize];
        byte[] title = memo.Title.getBytes();
        byte[] content = memo.Content.getBytes();

        int i;
        for (i = 0; i < title.length; i++)
            buf[i] = title[i];
        for (i = 0; i < content.length; i++)
            buf[TitleSize + i] = content[i];

        try {
            RandomAccessFile file = new RandomAccessFile(this.datafile, "rw");
            try {
                Header header = new Header(); 
                if (!readHeader(file, header))
                    return false;

                int pos = HeaderSize + header.RecordCount * DataSize;

                header.RecordCount++;
                writeHeader(file, header);

                file.seek(pos);    
                file.write(buf);
                file.writeChar(' ');

                return true;
            } finally {
                file.close();
            } 
        } catch (Exception e) {
            System.out.printf("\n>> 데이터 파일 헤더 저장 중 오류 발생 : %s\n", e.getMessage());
            return false;
        }
    }

    public int getMemoNo(String MemoSelect) throws Exception {
        Header header = new Header();
        int MemoNo = 0;

        try { 
            RandomAccessFile file = new RandomAccessFile(this.datafile, "rw");
            MemoNo = Integer.valueOf(MemoSelect);
            readHeader(file, header);
            if (MemoNo > header.RecordCount || MemoNo < 0) {
                System.out.println(">> 잘못된 입력입니다. 다시 시도하세요.");
                return -1;
            }
            else
                return Integer.valueOf(MemoNo);
        } catch (Exception e) {
            return -1;
        }
    }

    public int getMemoTitle(RandomAccessFile file, String MemoSelect) {
        try {
            MemoItem memo = new MemoItem();
            Header header = new Header();
            int readNo = 0;
            int i = 0;
            int j;
            String MemoTitle = " ";

            readHeader(file, header);
            file.seek(HeaderSize);
            while (i < header.RecordCount) {
                readMemo(file, memo, i);
                for (j = 0; j < MemoSelect.length(); j++) 
                    MemoTitle += memo.Title.charAt(j);
                MemoTitle = MemoTitle.trim();
                if (MemoSelect.equals(MemoTitle)) {
                    readNo = i;
                    return readNo;
                }
                i++;
            }
            return -1;
        } catch (Exception e) {
            return -1;
        }
    }

    public int selectMemo(String str) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String Type = " ";
        String MemoSelect = " ";
        String cmdLine = " ";
        int i;
        String [] AryStr = new String[100];

        try {
            while (true) {
                System.out.printf("\n어떤 메모를 %s 하시겠습니까? (번호 입력 : n [번호], 제목 입력 : t [제목])\n> ", str);
                cmdLine = br.readLine();
                AryStr = cmdLine.split(" ");
                Type = AryStr[0];
                for (i = 1; i < AryStr.length; i++) {
                    MemoSelect += AryStr[i];
                    MemoSelect += " ";
                }
                MemoSelect = MemoSelect.trim();

                if (Type.equals("n"))
                    return getMemoNo(MemoSelect);
                else if (Type.equals("t")) {
                    RandomAccessFile file = new RandomAccessFile(this.datafile, "rw");
                    return getMemoTitle(file, MemoSelect);
                }
                else {
                    System.out.printf(">> 잘못된 입력 : %s\n", cmdLine);
                    continue;
                }
            }
        } catch (Exception e) {
            System.out.printf("메모 검색 종류 입력 받는 중 오류 발생 : %s", e.getMessage());
            return -1;
        }
    }

    public boolean readMemo(RandomAccessFile file, MemoItem memo, int recNo) {
        byte[] title = new byte[TitleSize];
        byte[] content = new byte[ContentSize];
        
        try {
            file.seek(HeaderSize + DataSize * recNo);
            file.read(title);
            file.read(content);
            memo.isDeleted = file.readChar();
            
            memo.Title = new String(title);
            memo.Content = new String(content);

            return true;
        }
        catch (Exception e) {
            System.out.printf("\n>> 데이터 파일 읽는 중 오류 발생  : %s\n", e.getMessage());
            return false;
        }
    }

    public boolean deleteMemo(int recNo) {    
        try {
            RandomAccessFile file = new RandomAccessFile(this.datafile, "rw");
            Header header = new Header();

            file.seek(HeaderSize + recNo * DataSize + (TitleSize + ContentSize));
            file.writeChar('*');
            
            readHeader(file, header);
            file.seek(0);
            header.DeleteCount++;
            writeHeader(file, header);
            
            file.close();
            
            System.out.printf("\n>> %d번 메모가 정상적으로 삭제되었습니다.\n", recNo);    
        } 
        catch(Exception e) { 
            System.out.printf("\n>> 메모 삭제 중 오류 발생  : %s\n", e.getMessage());
        }
        return false;
    }

    public int showMemoList() {
        Header header = new Header();
        MemoItem memo = new MemoItem();
        int readCount = 0;

        try {  
            RandomAccessFile file = new RandomAccessFile(this.datafile, "rw");
            try {
                if (!readHeader(file, header))
                    return 0;
            
                System.out.printf("\n[메모 목록]\n");
                
                for (int i = 0; i < header.RecordCount; i++) {
                    if (!readMemo(file, memo, i))
                        break;

                    if (memo.isDeleted == '*') {
                        System.out.printf("%d. (삭제된 메모입니다.)\n", i);   
                        continue;
                    }

                    readCount++;
                    System.out.printf("%d. %s\n", i, memo.Title);
                }
                if (readCount > 0) 
                    System.out.printf("\n>> %d건 조회됨\n\n", readCount);
                else {
                    System.out.printf("\n>> 저장된 메모가 없습니다. (현재 저장된 메모 0건)\n\n");
                    return -1;
                }
            } 
            finally {
                file.close();
            } 
            return 0;
        } 
        catch (Exception e) {
            System.out.printf("\n>> 데이터 파일 읽는 중 오류 발생 : %s\n", e.getMessage());
            return 0;
        }
    }
    
    public boolean editMemo(int recNo) {
        try {
            RandomAccessFile file = new RandomAccessFile(this.datafile, "rw");
            DataInput input = new DataInput();
            MemoItem memo = new MemoItem();
            byte[] title = new byte[DataSize];
            byte[] content = new byte[ContentSize];

            System.out.printf("\n----- %d번 메모 편집 -----\n", recNo);
            System.out.printf("\n<기존 %d번 메모>\n", recNo);
            if(!(showMemo(recNo))) {
                file.close();
                return false;
            }

            System.out.println("\n새로운 메모 내용\n");
            input.getMemoData(memo);
            title = memo.Title.getBytes();
            content = memo.Content.getBytes();
            file.seek(0);    
            file.seek(HeaderSize + recNo * DataSize);
            file.write(title);
            file.seek(0);
            file.seek(HeaderSize + recNo * DataSize + TitleSize);
            file.write(content);
            file.close();        

        } catch (Exception e) {
            System.out.printf(">> 메모 편집 중 오류 발생 : %s\n", e.getMessage());
        }
        System.out.printf("\n>> 정상적으로 메모가 편집되었습니다.\n");
        return true;
    }

    public boolean showMemo(int MemoNo) {
        Header header = new Header();
        MemoItem memo = new MemoItem();

        try {
            RandomAccessFile file = new RandomAccessFile(this.datafile, "r");
            
            if (!(readHeader(file, header)))
                return false;

            if (MemoNo < 0 || MemoNo > header.RecordCount) {
                System.out.println("잘못된 번호입니다. 다시 시도하세요");
                return false;
            }

            readMemo(file, memo, MemoNo);
            if (memo.isDeleted == '*') {
                System.out.printf(">> 삭제된 메모입니다.\n", MemoNo);
                return false;
            }
            System.out.printf("\n[제목]\n%s", memo.Title);
            System.out.printf("\n\n[내용]\n%s", memo.Content);
            file.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}

class Account {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    RandomAccessFile file;
    String Name = " ";
    String Tel = " ";
    String PersonID = " ";
    String Password = " ";
    final int IdSize = 10;
    final int PwdSize = 4;
    final int NameSize = 20;
    final int TelSize = 20;

    public Account(String filename) {
        try {
            file = new RandomAccessFile(filename, "rw");
        } catch (IOException e) {
            System.out.printf("Account 파일 생성 중 오류 발생 : %s\n", e.getMessage());
        }
    }

    public Account() {
    }

    private void getPersonInfo() throws IOException {
        System.out.print("이름 : ");
        Name = br.readLine();
        System.out.print("전화번호('-' 없이 입력) : ");
        Tel = br.readLine();
    }

    public byte[] readAccount() throws IOException {
        byte [] buf = new byte[NameSize + TelSize + IdSize + PwdSize];
        int i = 0, c;

        file.seek(0);
        while(i < buf.length) {
            c = file.read();
            if (c == -1)
                break;
            buf[i] = (byte)c;
            i++;
        }
        return buf;        
    }

    public void writeAccount() throws IOException {
        byte [] name = Name.getBytes();
        byte [] tel = Tel.getBytes();
        byte [] perId = PersonID.getBytes();
        byte [] pwd = Password.getBytes();
        byte [] buf = new byte[NameSize + TelSize + IdSize + PwdSize];

        for (int i = 0; i < name.length; i++) 
            buf[i] = name[i];
        for (int i = 0; i < tel.length; i++)
            buf[NameSize + i] = tel[i];
        for (int i = 0; i < perId.length; i++)
            buf[NameSize + TelSize + i] = perId[i];
        for (int i = 0; i < pwd.length; i++) 
            buf[NameSize + TelSize + IdSize + i] = pwd[i];
        
        file.seek(0);
        file.write(buf);
    }

    public boolean makeAccount() throws IOException {
        try {
            System.out.println("\n----- 계정 생성 -----\n");
            
            getPersonInfo();
            System.out.print("ID(10자리 이내) : ");
            PersonID = br.readLine();
            if (PersonID.length() > 10 || PersonID.length() < 1) {
                System.out.println(">> ID는 10자리이내여야 합니다. 다시 시도하세요.");
                return false;
            }
            System.out.print("비밀번호(4자리) : ");
            Password = br.readLine();
            if (Password.length() > 4 || Password.length() < 1) {
                System.out.println(">> 비밀번호는 4자리여야 합니다. 다시 시도하세요.");
                return false;
            }
            writeAccount();
        } catch (Exception e) {
            System.out.printf("\n>> 계정 생성 중 오류 발생 : %s\n", e.getMessage());
        }
        System.out.println("\n>> 정상적으로 계정이 생성되었습니다.\n");
        return true;
    }

    public void editPersonInfo() {        
        byte[] buf = new byte[NameSize + TelSize + IdSize + PwdSize];
        Name = " ";
        Tel = " ";

        try {
            System.out.println("\n----- 개인정보 변경 -----");
            buf = readAccount();
            for (int i = 0; i < NameSize; i++)
                Name += (char)buf[i];
            for (int i = 0; i < TelSize; i++)
                Tel += (char)buf[NameSize + i];
            System.out.println("\n[기존 개인정보]\n");
            System.out.printf("이름 : %s\n전화번호 : %s\n", Name, Tel);

            System.out.println("\n변경하실 정보를 새로 입력해주세요 >\n");
            getPersonInfo();
            writeAccount();       
        } catch (Exception e) {
            System.out.printf(">> 개인정보 변경 중 오류 발생 : %s\n", e.getMessage());
        }
        System.out.println("\n>> 정상적으로 개인정보가 변경되었습니다.\n");
    }

    public void findPassword() {   
        byte[] buf = new byte[NameSize + TelSize + IdSize + PwdSize];
        Name = " ";
        Tel = " ";
        PersonID = " ";
        Password = " ";
        String sId = " ";
        String sName = " ";
        String sTel = " ";

        try {
            buf = readAccount();
            for (int i = 0; i < NameSize; i++)
                Name += (char)buf[i];
            for (int i = 0; i < TelSize; i++)
                Tel += (char)buf[NameSize + i];
            for (int i = 0; i < IdSize; i++)
                PersonID += (char)buf[NameSize + TelSize + i];
            for (int i = 0; i < PwdSize; i++)
                Password += (char)buf[NameSize + TelSize + IdSize + i];
            Name = Name.trim();
            Tel = Tel.trim();
            PersonID = PersonID.trim();
            Password = Password.trim();

            System.out.println("----- 비밀번호 찾기 -----\n");
            System.out.println(">> 비밀번호를 찾고자하는 ID를 입력해주세요.");
            System.out.print("> ID : ");
            sId = br.readLine();
            if (!(sId.equals(PersonID))) {
                System.out.println("\n>> 입력하신 ID를 찾을 수 없습니다.\n");
                return;
            }

            System.out.println("\n[본인인증]\n");
            System.out.print("이름 : ");
            sName = br.readLine();
            System.out.print("전화번호 : ");
            sTel = br.readLine();
            if (!(sName.equals(Name)) || !(sTel.equals(Tel))) {
                System.out.println("\n>> 계정에 등록된 개인정보와 일치하지 않습니다.\n");
                return;
            }

            System.out.println("\n[비밀번호 재설정]\n");
            System.out.print("새 비밀번호(4자리) : ");
            Password = br.readLine();
            if (Password.length() < 1 || Password.length() > 4) {
                System.out.println("\n>> 비밀번호는 4자리여야 합니다. 다시 시도하세요\n");
                return;
            }
            writeAccount();
        } catch (Exception e) {
            System.out.printf(">> 비밀번호 찾기 중 오류 발생 : %s", e.getMessage());
        }
        System.out.println("\n>> 정상적으로 비밀번호가 변경되었습니다.\n");
    }
}

interface LogIn {
    void inputLogIn() throws IOException;
    int excuteLogIn() throws IOException;
}

class MemoAccount extends Account implements LogIn {
    String Id = " ";
    String Pwd = " ";

    public MemoAccount(String filename) {
        super(filename);
    }

    @Override
    public void inputLogIn() throws IOException {
        Console console = System.console();
        char[] cPwd;

        if (file.length() < 1) {
            System.out.println(">> 계정이 없습니다. 계정 생성 화면으로 넘어갑니다.");
            super.makeAccount();
        }
        System.out.println("[ 메모 로그인 ]\n");
        System.out.printf("ID : ");
        Id = br.readLine();
        System.out.printf("비밀번호 : ");   // 비밀번호 입력 시 화면상에 보이지 않습니다.
        cPwd = console.readPassword();
        Pwd = new String(cPwd);
    }

    @Override
    public int excuteLogIn() throws IOException {
        byte[] buf = new byte[NameSize + TelSize + IdSize + PwdSize];
        PersonID = " ";
        Password = " ";
        int logInNo = 0;
        Scanner scanner = new Scanner(System.in);

        try {
            buf = readAccount();
            for (int i = 0; i < IdSize; i++)
                PersonID += (char)buf[NameSize + TelSize + i];
            for (int i = 0; i < PwdSize; i++)
                Password += (char)buf[NameSize + TelSize + IdSize + i];
        
            PersonID = PersonID.trim();
            Password = Password.trim();

            if (Id.equals(PersonID)) {
                if (Pwd.equals(Password)) {
                    System.out.println("\n>> 로그인 완료\n\n");
                    return 1;
                }
            }   
            else {
                System.out.print("\n>> ID 또는 비밀번호가 일치하지 않습니다.\n");
                System.out.print("\n[로그인 취소 : 0, 재시도 : 1, 비밀번호 찾기 : 2] >> ");
                logInNo = scanner.nextInt();
                System.out.println();

                if (logInNo == 0) 
                    return -1;
                else if (logInNo == 1) 
                    return 0;
                else if (logInNo == 2) {
                    findPassword();
                    return 0;
                }
                else {
                    System.out.println(">> 잘못된 입력입니다. 다시 시도하세요.");
                    return 2;
                } 
            }
        } catch (Exception e) {
            System.out.printf("로그인 중 오류 발생 : %s", e.getMessage());
        }
        return 1;        
    }
}