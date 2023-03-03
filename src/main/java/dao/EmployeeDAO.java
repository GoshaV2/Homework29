package dao;

import model.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeDAO {
    void save(Employee employee); //operation for update and insert
    Optional<Employee> findById(long id);
    List<Employee> findAll();
    void delete(Employee employee);
}
