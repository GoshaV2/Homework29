package dao;

import model.City;
import model.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeDAOImpl implements EmployeeDAO {
    private final static String url = "jdbc:postgresql://localhost:5432/skypro";
    private final static String user = "postgres";
    private final static String password = "admin";
    private final static EmployeeDAOImpl instance = new EmployeeDAOImpl();
    private Connection connection;

    private EmployeeDAOImpl() {
        try {
            connection = getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public static EmployeeDAOImpl getInstance() {
        try {
            if (instance.connection.isClosed()) {
                instance.connection = getConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return instance;
    }

    @Override
    public void save(Employee employee) {
        if (employee.getId() == null) {
            saveCity(employee);
            String addEmployeeQuery = "insert into employee (first_name, last_name, gender, age, city_id) VALUES (?,?,?,?,?);";
            PreparedStatement preparedStatement = null;
            ResultSet addEmployee = null;
            try {
                preparedStatement = connection.prepareStatement(addEmployeeQuery, Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1, employee.getFirstName());
                preparedStatement.setString(2, employee.getLastName());
                preparedStatement.setString(3, employee.getGender());
                preparedStatement.setInt(4, employee.getAge());
                if (employee.getCity() != null) {
                    preparedStatement.setLong(5, employee.getCity().getId());
                } else {
                    preparedStatement.setNull(5, Types.NULL);
                }
                preparedStatement.executeUpdate();

                addEmployee = preparedStatement.getGeneratedKeys();
                if (addEmployee.next()) {
                    employee.setId(addEmployee.getLong(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                    if (addEmployee != null) {
                        addEmployee.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            saveCity(employee);
            String updateEmployeeQuery = "update employee set first_name=?, last_name=?, gender=?, age=?, city_id=? where id=?";
            PreparedStatement preparedStatement = null;
            try {
                preparedStatement = connection.prepareStatement(updateEmployeeQuery);
                preparedStatement.setString(1, employee.getFirstName());
                preparedStatement.setString(2, employee.getLastName());
                preparedStatement.setString(3, employee.getGender());
                preparedStatement.setInt(4, employee.getAge());
                if (employee.getCity() != null) {
                    preparedStatement.setLong(5, employee.getCity().getId());
                } else {
                    preparedStatement.setNull(5, Types.NULL);
                }

                preparedStatement.setLong(6, employee.getId());

                preparedStatement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveCity(Employee employee) {
        if (employee.getCity() != null && employee.getCity().getId() == null) {
            String insertCityQuery = "insert into city (name) VALUES (?)";
            PreparedStatement addCity = null;
            ResultSet cityId = null;
            try {
                addCity = connection.prepareStatement(insertCityQuery, Statement.RETURN_GENERATED_KEYS);
                addCity.setString(1, employee.getCity().getName());
//                System.out.println(addCity.executeUpdate());
                addCity.executeUpdate();
                cityId = addCity.getGeneratedKeys();
                if (cityId.next()) {
                    employee.getCity().setId(cityId.getInt(1));
                }


            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (addCity != null) {
                        addCity.close();
                    }
                    if (cityId != null) {
                        cityId.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else if (employee.getCity() != null) {
            String insertCityQuery = "update  city set name =? where id=?";
            PreparedStatement update = null;
            try {
                update = connection.prepareStatement(insertCityQuery);
                update.setString(1, employee.getCity().getName());
                update.setLong(2, employee.getCity().getId());
                update.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (update != null) {
                        update.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Optional<Employee> findById(long id) {
        Employee employee = null;
        City city = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String findQuery = "select *  from employee left join city c on c.id = employee.city_id where employee.id=? fetch first 1 rows only";
            preparedStatement = connection.prepareStatement(findQuery);
            preparedStatement.setLong(1, id);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                employee = setEmployeeFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return Optional.ofNullable(employee);
    }

    private Employee setEmployeeFromResultSet(ResultSet resultSet) throws SQLException {
        Employee employee = new Employee();
        City city = null;
        employee.setId(resultSet.getLong("id"));
        employee.setFirstName(resultSet.getString("first_name"));
        employee.setLastName(resultSet.getString("last_name"));
        employee.setAge(resultSet.getInt("age"));
        employee.setGender(resultSet.getString("gender"));
        long cityId = resultSet.getLong("city_id");
        if (cityId != 0) {
            city = new City();
            city.setId(cityId);
            city.setName(resultSet.getString("name"));
        }
        employee.setCity(city);
        return employee;
    }

    @Override
    public List<Employee> findAll() {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Employee> employees = new ArrayList<>();
        try {
            String getAllQuery = "select *  from employee left join city c on c.id = employee.city_id";
            preparedStatement = connection.prepareStatement(getAllQuery);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Employee employee = setEmployeeFromResultSet(resultSet);
                employees.add(employee);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return employees;
    }

    @Override
    public void delete(Employee employee) {
        PreparedStatement preparedStatement = null;
        try {
            String deleteQuery = "delete from employee where id=?";
            preparedStatement = connection.prepareStatement(deleteQuery);
            preparedStatement.setLong(1, employee.getId());
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
