package com.isgc.portal.employee;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
  @Query("select e from Employee e where e.user.id = :userId")
  Optional<Employee> findByUserId(@Param("userId") UUID userId);

  @Query("select e from Employee e where e.primaryManager.id = :managerId")
  List<Employee> findDirectReports(@Param("managerId") UUID managerId);

  Optional<Employee> findByEmployeeNo(String employeeNo);
}


