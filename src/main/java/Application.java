import dao.EmployeeDAO;
import dao.EmployeeDAOImpl;
import model.City;
import model.Employee;

import java.sql.*;

public class Application {
    private final static String dbUrl="jdbc:postgresql://localhost:5432/skypro";
    public static void main(String[] args) throws SQLException {
        System.out.println("----Task 1----");
        task1();
        System.out.println("----Task 2----");
        task2();
    }

    private  static void task1() throws SQLException {
        Connection connection=DriverManager.getConnection(dbUrl,"postgres","admin");
        PreparedStatement preparedStatement=connection.prepareStatement("select * from employee inner join city c on c.id = employee.city_id  where employee.id=?");
        preparedStatement.setInt(1,2);
        preparedStatement.execute();
        ResultSet resultSet=preparedStatement.getResultSet();
        while (resultSet.next()){
            System.out.println(resultSet.getString("first_name"));
            System.out.println(resultSet.getString("last_name"));
            System.out.println(resultSet.getString("gender"));
            System.out.println(resultSet.getString("name"));
        }
    }

    private static void task2(){
        EmployeeDAO employeeDAO=EmployeeDAOImpl.getInstance();

        Employee employeeCreated=new Employee();
        City city=new City();
        city.setName("ТЕст");
        employeeCreated.setAge(12);
        employeeCreated.setFirstName("test");
        employeeCreated.setLastName("test");
        employeeCreated.setGender("test");
        employeeCreated.setCity(city);
        employeeDAO.save(employeeCreated);
        System.out.println(employeeCreated);

        Employee employee=employeeDAO.findById(employeeCreated.getId()).orElseThrow();
        System.out.println(employee);
        employeeDAO.delete(employee);
        System.out.println(employeeDAO.findById(employee.getId()).isEmpty());

        employeeDAO.save(employee);

        System.out.println(employeeDAO.findAll());
    }
}
