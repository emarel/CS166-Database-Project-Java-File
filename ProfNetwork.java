/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class ProfNetwork {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of ProfNetwork
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public ProfNetwork (String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end ProfNetwork

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
	 if(outputHeader){
	    for(int i = 1; i <= numCol; i++){
		System.out.print(rsmd.getColumnName(i) + "\t");
	    }
	    System.out.println();
	    outputHeader = false;
	 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
          List<String> record = new ArrayList<String>();
         for (int i=1; i<=numCol; ++i)
            record.add(rs.getString (i));
         result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       if(rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            ProfNetwork.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      ProfNetwork esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the ProfNetwork object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new ProfNetwork (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. Goto Friend List");
                System.out.println("2. Update Profile");
                System.out.println("3. Write a new message");
                System.out.println("4. Send Friend Request");
                System.out.println("5. View Profile");
                System.out.println("6. Find Profile");
                System.out.println("7. View Friend Requests");
                System.out.println(".........................");
                System.out.println("9. Log out");
                switch (readChoice()){
//havent coded for yet
                   case 1: listOfFriends(esql,authorisedUser); break;
                   case 2: UpdateProfile(esql,authorisedUser); break;
                   //case 3: NewMessage(esql); break;
                   case 4: SendRequest(esql, authorisedUser); break;
                   case 5: viewProfile(esql, authorisedUser); break;
                   case 6: findProfile(esql, authorisedUser); break;
                   case 7: PendingRequests(esql, authorisedUser); break;
                   case 9: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    * An empty block and contact list would be generated and associated with a user
    **/
   public static void CreateUser(ProfNetwork esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user email: ");
         String email = in.readLine();
         System.out.print("\tEnter full name: ");
         String fname = in.readLine();
         System.out.print("\tEnter date of birth - Year/Month/Date - xxxx/xx/xx: " );
         String dob = in.readLine();

	 //Creating empty contact\block lists for a user
	 String query = String.format("INSERT INTO USR (userId, password, email, name, dateOfBirth) VALUES ('%s','%s','%s', '%s', '%s')", login, password, email, fname, dob);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end

   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(ProfNetwork esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USR WHERE userId = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// Rest of the functions definition go in here
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

 public static void UpdateProfile(ProfNetwork esql, String authorisedUser){

        System.out.println("UPDATE PROFILE");
        System.out.println("---------");
        System.out.println("1. Change Password");
        System.out.println("2. Update Educational Details");
        System.out.println("3. Update Work Experience");
        System.out.println("4. Update Email");
        
        //System.out.println("9. Return to Main Menu");
        

        switch (readChoice()){

           case 1: ChangePassword(esql,authorisedUser);
           break;          
           case 2: UpdateEducation(esql, authorisedUser); 
           break;
           case 3: CreateUser(esql);
           break;
           case 4: updateEmail(esql, authorisedUser);
           break;
           //case 9: keepon = false; break;
           default : System.out.println("Unrecognized choice!"); break;
        }
      }

      public static void ChangePassword(ProfNetwork esql, String authorisedUser){

        System.out.println("\tCHANGE PASSWORD");
        System.out.println("---------");

        try{
          
          System.out.print("\tEnter current password: ");
          String old_password = in.readLine();
          System.out.print("\tEnter new password: ");
          String new_password = in.readLine();

         String query = String.format("UPDATE USR SET password = '%s' WHERE userId = '%s'", new_password, authorisedUser);
          esql.executeUpdate(query);
          System.out.print("\tPASSWORD CHANGED! ");

           }
        catch(Exception e){
         System.err.println (e.getMessage ());
          }

        
      }
      
      public static void UpdateEducation(ProfNetwork esql, String authorisedUser){

        System.out.println("\tUPDATE EDUCATION");
        System.out.println("---------");

        try{
          
          System.out.print("\tEnter name of College/University: ");
          String school = in.readLine();
          System.out.print("\tEnter major: ");
          String major = in.readLine();
          System.out.print("\tEnter Degree: ");
          String degree = in.readLine();
          System.out.print("\tEnter start date (YYYY/MM/DD): ");
          String start = in.readLine();
          System.out.print("\tEnter end date (YYYY/MM/DD): ");
          String end = in.readLine();
          String query = String.format("INSERT INTO educational_details(userId, instituitionName, major, degree, startdate, enddate) values('%s','%s','%s', '%s', '%s', '%s')", authorisedUser, school, major, degree, start, end);
          esql.executeUpdate(query);
          
         
  
         
          System.out.print("\tEDUCATION UPDATED! ");

           }
        catch(Exception e){
         System.err.println (e.getMessage ());
          }

        
      }
      
      public static void updateEmail(ProfNetwork esql, String authorisedUser){

        System.out.println("\tUPDATE EMAIL");
        System.out.println("---------");

        try{
          
          System.out.print("\tEnter new email: ");
          String emailUpdate = in.readLine();
          String query = String.format("UPDATE usr set email = '%s' WHERE userid = '"+authorisedUser+"'",emailUpdate);
			esql.executeUpdate(query);
        
          
         
  
         
          System.out.print("\tEMAIL UPDATED! ");

           }
        catch(Exception e){
         System.err.println (e.getMessage ());
          }

        
      }
      
    public static void listOfFriends(ProfNetwork esql, String authorisedUser){
	    System.out.println("\tLIST OF FRIENDS");
       System.out.println("---------");
     try{
       
       
      

        
       List<List<String>> listFriends1 = new ArrayList<List<String>>();
       String query = String.format("SELECT connectionid AS userid FROM connection_usr WHERE userID = '%s' AND status	= '%s' UNION ALL SELECT userid FROM connection_usr WHERE connectionid = '%s' AND status= '%s'", 
						   authorisedUser, "Accept", authorisedUser, "Accept");
       listFriends1 = esql.executeQueryAndReturnResult(query);
       if(listFriends1.isEmpty()){
				System.out.println("You have no connections at this time\n");
			}
       
       System.out.println("\nList of Friends/Connections: ");
       int num;
       for(int i = 0; i < listFriends1.size(); ++i){
            num = i + 1;
						System.out.println(""+num+": " + ""+listFriends1.get(i).get(0)+"");
					}
          
          System.out.println("\t1. View a profile");
					System.out.println("\t9. Got to main menu\n");
        int choice;
       switch(esql.readChoice()){
						case 1: System.out.print("Please enter the number of the connection you wish to view:");
								choice = Integer.parseInt(esql.in.readLine().trim()) - 1;
								System.out.println();
              //////////////////////////////////////////////		
              //
              // VIEW PROFILE
              //
              //
              //
              //prof.ViewUserProfile(esql, currentUser, result.get(usrChoice).get(0));
								break;
						case 9:  break;
						default: System.out.println("Try again");	
       }
     
               
               
       
       
	   }
     catch(Exception e){
		   System.err.println(e.getMessage());
		   
	   }
   }
      
      
    public static void findProfile(ProfNetwork esql, String authorisedUser){

        System.out.println("\tFIND A PROFILE");
        System.out.println("---------");

        try{
          
          System.out.print("\tPlease enter User ID: ");
          String username = in.readLine();
          
          String query = String.format("SELECT * FROM USR WHERE userId = '%s'", username);
          int results = esql.executeQuery(query);
          if(results == 0){
            System.out.print("\tUsername not found ");
          }
          else{
            //DISPLAY PROFILE NOT COMPLETE;
            System.out.print("\tUser found ");
          }


        
      }
       catch(Exception e){
         System.err.println (e.getMessage ());
        }
    }
		
	public static void viewProfile(ProfNetwork esql, String authorisedUser){

	   String query = String.format("SELECT email, name, dateOfBirth FROM USR WHERE userId='" +authorisedUser + "'");
	   List<List<String> > udetails = new ArrayList<List<String> >();
	   try{
	       System.out.println(ANSI_GREEN + "\n\n-----------USER PROFILE-----------" + ANSI_RESET);
		   udetails = esql.executeQueryAndReturnResult(query);
		   System.out.println("\nNAME: " + udetails.get(0).get(1) + "");
	   	   System.out.println("Email: " + udetails.get(0).get(0) + "");
		   System.out.println("Date of Birth: " + udetails.get(0).get(2) + "");
           System.out.println("----------------------------------");
	   }catch(Exception e){
		   System.err.println(e.getMessage());
	   }

	   query = String.format("SELECT company, role, location, startdate, enddate FROM WORK_EXPR WHERE userId='" + authorisedUser + "'");
	   List<List<String> > wdetails = new ArrayList<List<String> >();
	   try{
		   wdetails = esql.executeQueryAndReturnResult(query);
		   System.out.println(ANSI_GREEN + "\n----------WORK EXPERIENCE---------\n" + ANSI_RESET);
		   if(wdetails.isEmpty()){
		       System.out.println(ANSI_RED + "\nNo Work Experience" + ANSI_RESET);
		   }
	       else{
		       for(int i=0; i<wdetails.size(); i++){
				   System.out.println("\nCompany: " + wdetails.get(i).get(0) + "");
			       System.out.println("Role: " + wdetails.get(i).get(1) + "");
			       System.out.println("Location: " + wdetails.get(i).get(2) +"");
			       System.out.println("Start Date: " + wdetails.get(i).get(3) +"");
			       System.out.println("End Date: " + wdetails.get(i).get(4) +"");
			   }
	      }
	      System.out.println("----------------------------------");
	   }catch(Exception e){
		   System.err.println(e.getMessage());
	   }

	   query = String.format("SELECT instituitionName, major, degree, startdate, enddate FROM EDUCATIONAL_DETAILS WHERE userId='" + authorisedUser + "'");
	   List<List<String> > edetails = new ArrayList<List<String> >();
	   try{
		   edetails = esql.executeQueryAndReturnResult(query);
		   System.out.println(ANSI_GREEN + "\n-------EDUCATION EXPERIENCE-------" + ANSI_RESET);
	       if(edetails.isEmpty()){
	           System.out.println(ANSI_RED + "\nNo Education Experience\n" + ANSI_RESET);
	       }
            else{
			   for(int i=0; i<edetails.size(); i++){
				   System.out.println("\nInstitution Name: " + edetails.get(i).get(0) + "");
				   System.out.println("Major: " + edetails.get(i).get(1) + "");
				   System.out.println("Degree: " + edetails.get(i).get(2) + "");
				   System.out.println("Start Date: " + edetails.get(i).get(3) + "");
				   System.out.println("End Date: " + edetails.get(i).get(4) + "");
			   }
		   }
		   System.out.println("----------------------------------");
	   }catch(Exception e){
		   System.err.println(e.getMessage());
	   }
   }//end
   
   public static void SendRequest(ProfNetwork esql, String authorisedUser){
       System.out.print("Enter the userid of the person to connect with:");
       try{
           String uidreq = esql.in.readLine();
           String query = String.format("INSERT INTO connection_usr (userId, connectionId, status) " + "VALUES('"+authorisedUser+"', '"+uidreq+"', 'Request')");
           esql.executeUpdate(query);
           System.out.println("Your request has been sent to " + uidreq);
       } catch(Exception e){
           System.err.println(e.getMessage());
       }
   }
   
   public static void PendingRequests(ProfNetwork esql, String authorisedUser){
       List<List<String>> cdetails = new ArrayList<List<String>>();
       try{
           String query = String.format("SELECT userid FROM connection_usr WHERE connectionid = '"+authorisedUser+"' AND status = 'Request'");
           cdetails = esql.executeQueryAndReturnResult(query);
           if(cdetails.isEmpty()){
               System.out.println("You have no connection requests.");
           }
           else{
               System.out.println("Connection Requests: ");
               int count = 1;
               for(int i = 0; i < cdetails.size(); i++){
						System.out.println(""+count+". " + ""+cdetails.get(i).get(0)+"");
						count++;
			    }
            System.out.println("\n\n---------");
            System.out.println("1. Accept Request");
            System.out.println("2. Deny Request");
            int menusel = 0;
            switch (readChoice()){
               case 1:  System.out.print("Please enter the number of the connection to accept: ");
                        menusel = Integer.parseInt(esql.in.readLine().trim())-1;
                        query = String.format("UPDATE connection_usr SET status = 'Accept' WHERE userid = '"+cdetails.get(menusel).get(0)+"' AND connectionid = '"+authorisedUser+"'");
                        try{
                            esql.executeUpdate(query);
                        } catch(Exception e){
                            System.err.println(e.getMessage());
                        }
                        break;
               case 2:  System.out.print("Please enter the number of the connection to deny: ");
                        menusel = Integer.parseInt(esql.in.readLine().trim())-1;
                        query = String.format("UPDATE connection_usr SET status = 'Reject' WHERE userid = '"+cdetails.get(menusel).get(0)+"' AND connectionid = '"+authorisedUser+"'");
                        try{
                            esql.executeUpdate(query);
                        } catch(Exception e){
                            System.err.println(e.getMessage());
                        }
                        break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
           }
       } catch(Exception e){
           System.out.println(e.getMessage());
       }
   }
   
public static final String ANSI_RESET = "\u001B[0m";
public static final String ANSI_BLACK = "\u001B[30m";
public static final String ANSI_RED = "\u001B[31m";
public static final String ANSI_GREEN = "\u001B[32m";
public static final String ANSI_YELLOW = "\u001B[33m";
public static final String ANSI_BLUE = "\u001B[34m";
public static final String ANSI_PURPLE = "\u001B[35m";
public static final String ANSI_CYAN = "\u001B[36m";
public static final String ANSI_WHITE = "\u001B[37m";
		
/*  
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user email: ");
         String email = in.readLine();
         System.out.print("\tEnter full name: ");
         String fname = in.readLine();
         System.out.print("\tEnter date of birth - Year/Month/Date - xxxx/xx/xx: " );
         String dob = in.readLine();

   //Creating empty contact\block lists for a user
   String query = String.format("INSERT INTO USR (userId, password, email, name, dateOfBirth) VALUES ('%s','%s','%s', '%s', '%s')", login, password, email, fname, dob);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
      */
  




}//end ProfNetwork
