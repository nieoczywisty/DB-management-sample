package com.obd;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
	static String url = "jdbc:oracle:thin:@//ora4.ii.pw.edu.pl:1521/pdb1.ii.pw.edu.pl";
	static String user = "username";
	static String password = "password";
	static String driverName = "oracle.jdbc.driver.OracleDriver";

	static String sqlPrzedmiot = "CREATE TABLE przedmiot (" + "idp integer not null,"
			+ "nazwa_przedmiotu char(30) not null)";
	static String sqlNauczyciel = "CREATE TABLE nauczyciel (" + "idn integer not null,"
			+ "nazwisko_nauczyciela char(30) not null," + "imie_nauczyciela char(20) not null)";
	static String sqlUczen = "CREATE TABLE uczen (" + "idu integer not null," + "nazwisko_ucznia char(30) not null,"
			+ "imie_ucznia char(20) not null)";
	static String sqlOcenianie = "CREATE TABLE ocenianie (" + "idn integer not null," + "idu integer not null,"
			+ "idp integer not null," + "ido integer not null," + "rodzaj_oceny char(1) not null)";
	static String sqlOcena = "CREATE TABLE ocena (" + "ido integer not null," + "wartość_opisowa char(20) not null,"
			+ "wartość_numeryczna float not null )";
	static Statement statement = null;
	static String[] scannerInputData;
	static boolean exitProgram = false;
	static boolean verifyInputError = false;
	static boolean validateInputError = false;
	static String input = "";
	static ResultSet rs4;
	static ResultSet rs3;
	static ResultSet rs2;
	static ResultSet rs1;
	static Scanner scanner;
	static ResultSet rsNAUCZYCIEL;
	static ResultSet rsPRZEDMIOT;
	static ResultSet rsOCENA;
	static ResultSet rsUCZEN;
	static ResultSet rsOCENIANIE;

	public static void main(String[] args) {

//weryfikacja sterownika, jesli brak to stop z komunikatem brak sterownika.
		if (!verifyOracleDriver()) {
			return;
		}
//sprawdzenie czy tabele zostały wcześniej utworzone, jesli nie to ich utworzenie
		try (Connection connection = DriverManager.getConnection(url, user, password)) {
			createDataBase(connection); // table creation
		} catch (Exception e) {
			exitProgram = true;
			e.printStackTrace();
		}

//obsługa wprowadzania danych do tabeli

		try (Connection connection = DriverManager.getConnection(url, user, password);
				Scanner scanner = new Scanner(System.in);
				PreparedStatement checkOceny = connection.prepareStatement("SELECT * FROM Ocena WHERE ido = ?");
				PreparedStatement checkPrzedmiot = connection.prepareStatement("SELECT * FROM Przedmiot WHERE idp = ?");
				PreparedStatement checkUczen = connection.prepareStatement("SELECT * FROM Uczen WHERE idu = ?");
				PreparedStatement checkNauczyciel = connection.prepareStatement("SELECT * FROM Nauczyciel WHERE idn = ?");) {

			while (!exitProgram) {
				scannerInputData = null;
				validateInputError = false;
				verifyInputError = false;
				getData(scanner);
				while (!exitProgram && scannerInputData != null) {
					verifyInput(scannerInputData);
					if (verifyInputError) {
						break;
					} else {
						validateInput(connection, scannerInputData, checkNauczyciel, checkOceny, checkPrzedmiot,
								checkUczen);
						if (validateInputError) {
							break;
						} else {
							insertIntoOcenianie(connection);
							break;
						}
					}
				}
			}

		} catch (Exception e) {
			System.out.println("Błąd programu!");
			e.printStackTrace();
			return;
		}
		System.out.println("Koniec programu");
	}

//		Tworzenie tabel
	private static void createDataBase(Connection connection) {
		try (Statement statement = connection.createStatement()) {

			DatabaseMetaData md = connection.getMetaData();
			rsNAUCZYCIEL = md.getTables(null, null, "NAUCZYCIEL", null);
			if (!rsNAUCZYCIEL.next()) {
				System.out.println(statement.execute(sqlNauczyciel));
				System.out.println("Tabela -Nauczyciel- utworzona");
				insertNauczyciel(connection);
				System.out.println("Insert -Nauczuciel- zakończony powodzeniem");
			}
			rsPRZEDMIOT = md.getTables(null, null, "PRZEDMIOT", null);
			if (!rsPRZEDMIOT.next()) {
				System.out.println(statement.execute(sqlPrzedmiot));
				System.out.println("Tabela -Przedmiot- utworzona");
				insertPrzedmiot(connection);
				System.out.println("Insert -Przedmiot- zakończony powodzeniem");
			}
			rsUCZEN = md.getTables(null, null, "UCZEN", null);
			if (!rsUCZEN.next()) {
				System.out.println(statement.execute(sqlUczen));
				System.out.println("Tabela -Uczen- utworzona");
				insertUczen(connection);
				System.out.println("Insert -Uczen- zakończony powodzeniem");
			}
			rsOCENA = md.getTables(null, null, "OCENA", null);
			if (!rsOCENA.next()) {
				System.out.println(statement.execute(sqlOcena));
				System.out.println("Tabela -Ocena- utworzona");
				insertOcena(connection);
				System.out.println("Insert -Ocena- zakończony powodzeniem");
			}
			rsOCENIANIE = md.getTables(null, null, "OCENIANIE", null);
			if (!rsOCENIANIE.next()) {
				System.out.println(statement.execute(sqlOcenianie));
				System.out.println("Tabela -Ocenianie- utworzona");

			} else {
				System.out.println("Tabele zostały już wcześniej utworzone.");
			}
			
			return;
			// statement zamykany przez blok try with resources
		} catch (SQLException e) {
			System.out.println("Exception!!");
			e.printStackTrace();
			return;
		}finally {
			try {
				rsNAUCZYCIEL.close();
				rsPRZEDMIOT.close();
				rsUCZEN.close();
				rsOCENA.close();
				rsOCENIANIE.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

//	Uzupełnianie Tabel danymi---------------------------------------------------------------------------------------

//	NAUCZYCIEL
	private static void insertNauczyciel(Connection connection) {

		String sqlIntoNauczyciel1 = "INSERT INTO nauczyciel (idn, nazwisko_nauczyciela, imie_nauczyciela) "
				+ "VALUES (1, 'Kowalski', 'Jan')";
		String sqlIntoNauczyciel2 = "INSERT INTO nauczyciel (idn, nazwisko_nauczyciela, imie_nauczyciela)"
				+ "VALUES (2, 'Duda', 'Sławomir')";
		String sqlIntoNauczyciel3 = "INSERT INTO nauczyciel (idn, nazwisko_nauczyciela, imie_nauczyciela)"
				+ "VALUES (3, 'Szklanka', 'Piotr')";

		try (Statement insertStatementNauczyciel = connection.createStatement()) {
			System.out.println("execute: " + insertStatementNauczyciel.executeUpdate(sqlIntoNauczyciel1));
			System.out.println("execute: " + insertStatementNauczyciel.executeUpdate(sqlIntoNauczyciel2));
			System.out.println("execute: " + insertStatementNauczyciel.executeUpdate(sqlIntoNauczyciel3));
			// statement zamykany przez try with resources
		} catch (SQLException e3) {
			System.out.println("Insert SQL EXception!");
			e3.printStackTrace();
			return;
		}
	}

//	PRZEDMIOT
	private static void insertPrzedmiot(Connection connection) {

		String sqlIntoPrzedmiot1 = "INSERT INTO przedmiot (idp, nazwa_przedmiotu) " + "VALUES (1, 'Matematyka')";
		String sqlIntoPrzedmiot2 = "INSERT INTO przedmiot (idp, nazwa_przedmiotu) " + "VALUES (2, 'Fizyka')";
		String sqlIntoPrzedmiot3 = "INSERT INTO przedmiot (idp, nazwa_przedmiotu) " + "VALUES (3, 'Informatyka')";

		try (Statement insertStatementPrzedmiot = connection.createStatement()) {
			System.out.println("execute: " + insertStatementPrzedmiot.executeUpdate(sqlIntoPrzedmiot1));
			System.out.println("execute: " + insertStatementPrzedmiot.executeUpdate(sqlIntoPrzedmiot2));
			System.out.println("execute: " + insertStatementPrzedmiot.executeUpdate(sqlIntoPrzedmiot3));
			// statement zamykany przez try with resources
		} catch (SQLException e4) {
			System.out.println("Insert przedmiot SQL EXception!");
			e4.printStackTrace();
			return;
		}
	}

//	UCZEŃ
	private static void insertUczen(Connection connection) {

		String sqlIntoUczen1 = "INSERT INTO uczen (idu, nazwisko_ucznia, imie_ucznia) "
				+ "VALUES (1, 'Smialy', 'Bolselaw')";
		String sqlIntoUczen2 = "INSERT INTO uczen (idu, nazwisko_ucznia, imie_ucznia) "
				+ "VALUES (2, 'Dlugi', 'Mikolaj')";
		String sqlIntoUczen3 = "INSERT INTO uczen (idu, nazwisko_ucznia, imie_ucznia) " + "VALUES (3, 'Pac', 'Kamila')";

		try (Statement insertStatementUczen = connection.createStatement()) {
			System.out.println("execute: " + insertStatementUczen.executeUpdate(sqlIntoUczen1));
			System.out.println("execute: " + insertStatementUczen.executeUpdate(sqlIntoUczen2));
			System.out.println("execute: " + insertStatementUczen.executeUpdate(sqlIntoUczen3));
			// statement zamykany przez try with resources
		} catch (SQLException e5) {
			System.out.println("Insert uczen SQL EXception!");
			e5.printStackTrace();
			return;
		}
	}

//	Ocena
	private static void insertOcena(Connection connection) {

		String sqlIntoOcena1 = "INSERT INTO ocena (ido, wartość_opisowa, wartość_numeryczna) "
				+ "VALUES (1, 'Bardzo Dobry', 5)";
		String sqlIntoOcena2 = "INSERT INTO ocena (ido, wartość_opisowa, wartość_numeryczna) "
				+ "VALUES (2, 'Dobry +', 4.5)";
		String sqlIntoOcena3 = "INSERT INTO ocena (ido, wartość_opisowa, wartość_numeryczna) "
				+ "VALUES (3, 'Dobry', 4)";
		String sqlIntoOcena4 = "INSERT INTO ocena (ido, wartość_opisowa, wartość_numeryczna) "
				+ "VALUES (4, 'Dostateczny +', 3.5)";
		String sqlIntoOcena5 = "INSERT INTO ocena (ido, wartość_opisowa, wartość_numeryczna) "
				+ "VALUES (5, 'Dostateczny', 3)";
		String sqlIntoOcena6 = "INSERT INTO ocena (ido, wartość_opisowa, wartość_numeryczna) "
				+ "VALUES (6, 'Dopuszczający +', 2.5)";
		String sqlIntoOcena7 = "INSERT INTO ocena (ido, wartość_opisowa, wartość_numeryczna) "
				+ "VALUES (7, 'Dopuszczający', 2)";
		String sqlIntoOcena8 = "INSERT INTO ocena (ido, wartość_opisowa, wartość_numeryczna) "
				+ "VALUES (8, 'Niedostateczny', 1)";

		try (Statement insertStatementOcena = connection.createStatement()) {
			System.out.println("execute: " + insertStatementOcena.executeUpdate(sqlIntoOcena1));
			System.out.println("execute: " + insertStatementOcena.executeUpdate(sqlIntoOcena2));
			System.out.println("execute: " + insertStatementOcena.executeUpdate(sqlIntoOcena3));
			System.out.println("execute: " + insertStatementOcena.executeUpdate(sqlIntoOcena4));
			System.out.println("execute: " + insertStatementOcena.executeUpdate(sqlIntoOcena5));
			System.out.println("execute: " + insertStatementOcena.executeUpdate(sqlIntoOcena6));
			System.out.println("execute: " + insertStatementOcena.executeUpdate(sqlIntoOcena7));
			System.out.println("execute: " + insertStatementOcena.executeUpdate(sqlIntoOcena8));
			// statement zamykany przez try with resources
		} catch (SQLException e6) {
			System.out.println("Insert ocena SQL EXception!");
			e6.printStackTrace();
			return;
		}
	}

	private static boolean verifyOracleDriver() {
		try {
			System.out.println("Sprawdzam Sterownik");
			Class<?> c = Class.forName(driverName);
			System.out.println("Pakiet       :" + c.getPackage());
			System.out.println("Nazwa klasy  :" + c.getName());
		} catch (Exception e) {
			System.out.println("Brak Sterownika!");
			return false;
		}
		System.out.println("Sterownik działa poprawnie");
		return true;
	}

	private static void getData(Scanner scanner) {
		System.out.println(
				"Podaj dane oddzielone spacjami w formacie idn idu idp ido oraz rodzaj oceny S lub C. Wpisz exit lub wciśnij ctrl + z aby wyjść.");
		try {
			input = new String(scanner.nextLine());

			String[] inValues = input.split(" ");
			scannerInputData = new String[inValues.length];
			for (int i = 0; i < scannerInputData.length; i++) {
				scannerInputData[i] = inValues[i];
			}

			// wyjscie z programu
			boolean contains = Arrays.stream(scannerInputData).anyMatch("exit"::equals);
			if (contains) {
				exitProgram = true;
			}

		} catch (NoSuchElementException e) {
			exitProgram = true;
			return;
		}
	}

	private static void verifyInput(String[] scannerInputData) {
		try {
			if (scannerInputData.length < 5) {
				System.out.println("Za mało danych!");
				verifyInputError = true;
			} else if (scannerInputData[0] == null || scannerInputData[1] == null || scannerInputData[2] == null
					|| scannerInputData[3] == null || scannerInputData[4] == null) {
				// sprawdzanie czy wartosc nie jest null lub ma niepoprawny format
				System.out.println("niepoprawny format danych wejściowych!");
				verifyInputError = true;
			} else if (!scannerInputData[0].matches("^\\d+$") || !scannerInputData[1].matches("^\\d+$")
					|| !scannerInputData[2].matches("^\\d+$") || !scannerInputData[3].matches("^\\d+$")
					|| scannerInputData[4].length() > 1) {
				// sprawdzanie czy dane są cyframi i poprawnym typem oceny
				System.out.println("niepoprawny format danych wejściowych!");
				verifyInputError = true;
			} else if (!"S".equals(scannerInputData[4]) && !"C".equals(scannerInputData[4])) {
				System.out.println("Podany rodzaj oceny nie istnieje, podaj poprawny S lub C:");
				verifyInputError = true;
				return;
			}
		} catch (NullPointerException e) {
			System.out.println("Wyjątek NPE @ verifyInput");
			e.printStackTrace();
			return;

		}
	}

	private static void validateInput(Connection connection, String[] scannerInputData,
			PreparedStatement checkNauczyciel, PreparedStatement checkOceny, PreparedStatement checkPrzedmiot,
			PreparedStatement checkUczen) throws SQLException {
		try (Statement statement = connection.createStatement();) {

			checkOceny.setInt(1, Integer.parseInt(scannerInputData[3]));
			checkPrzedmiot.setInt(1, Integer.parseInt(scannerInputData[2]));
			checkUczen.setInt(1, Integer.parseInt(scannerInputData[1]));
			checkNauczyciel.setInt(1, Integer.parseInt(scannerInputData[0]));
			rs4 = checkOceny.executeQuery();

			// Sprawdz czy ID wpisane istniej� | Symulacja kluczy

			if (rs4.next()) {
				rs3 = checkPrzedmiot.executeQuery();
				if (rs3.next()) {
					rs2 = checkUczen.executeQuery();
					if (rs2.next()) {
						rs1 = checkNauczyciel.executeQuery();
						if (rs1.next()) {
							
						} else {
							System.out.println("Podany nauczyciel NIE istnieje!");
							validateInputError = true;
						}
					} else {
						System.out.println("Podany uczen NIE istnieje!");
						validateInputError = true;
					}
				} else {
					System.out.println("Podany przedmiot NIE istnieje!");
					validateInputError = true;
				}
			} else {
				System.out.println("Podana ocena NIE istnieje!");
				validateInputError = true;
			}
			return;
		} catch (SQLException e) {
			System.out.println("SQL EXception");
			e.printStackTrace();
			return;
		} catch (NullPointerException e) {
			System.out.println("NPE validate");
			e.printStackTrace();
			return;
		}finally {
			rs1.close();
			rs2.close();
			rs3.close();
			rs4.close();
		}

	}

//	Wypelnianie tabeli ocenianie
	private static void insertIntoOcenianie(Connection connection) throws SQLException, IOException {

		try (Statement statement = connection.createStatement()) {
			String sql = "INSERT INTO Ocenianie Values" + " (" + Integer.parseInt(scannerInputData[0]) + ","
					+ Integer.parseInt(scannerInputData[1]) + "," + Integer.parseInt(scannerInputData[2]) + ","
					+ Integer.parseInt(scannerInputData[3]) + ",'" + scannerInputData[4] + "')";
			System.out.println(sql);
			System.out.println("Polecenie wykonane, kod : " + statement.executeUpdate(sql));

		} catch (Exception e) {
			System.out.println("Wyjątek @ InsertIntoOcenianie: ");
			e.printStackTrace();
			return;
		}
	}

}
