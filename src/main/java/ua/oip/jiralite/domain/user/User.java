package ua.oip.jiralite.domain.user;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import ua.oip.jiralite.domain.BaseEntity;
import ua.oip.jiralite.domain.Comment;
import ua.oip.jiralite.domain.Issue;
import ua.oip.jiralite.domain.Project;

@Entity
@Table(name = "users")
public class User extends BaseEntity {
    
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @Column(name = "full_name")
    private String fullName;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    
    @ManyToMany(mappedBy = "members")
    private Set<Project> projects = new HashSet<>();
    
    @OneToMany(mappedBy = "reporter")
    private Set<Issue> reportedIssues = new HashSet<>();
    
    @OneToMany(mappedBy = "assignee")
    private Set<Issue> assignedIssues = new HashSet<>();
    
    @OneToMany(mappedBy = "author")
    private Set<Comment> comments = new HashSet<>();
    
    @Transient
    private char[] passwordChars;
    
    public User() {
    }
    
    public User(String username) {
        this.username = username;
    }
    
    public User(String username, String password, String email, String fullName) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
        this.passwordChars = null;
    }
    
    public char[] getPasswordChars() {
        if (passwordChars == null && password != null) {
            passwordChars = password.toCharArray();
        }
        return passwordChars;
    }
    
    public void setPasswordChars(char[] passwordChars) {
        this.passwordChars = passwordChars;
        this.password = new String(passwordChars);
    }
    
    public void clearPasswordChars() {
        if (passwordChars != null) {
            Arrays.fill(passwordChars, '\0');
            passwordChars = null;
        }
    }
    
    public boolean checkPassword(char[] inputPassword) {
        return this.password != null && this.password.equals(new String(inputPassword));
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public Set<Project> getProjects() {
        return projects;
    }
    
    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }
    
    public Set<Issue> getReportedIssues() {
        return reportedIssues;
    }
    
    public void setReportedIssues(Set<Issue> reportedIssues) {
        this.reportedIssues = reportedIssues;
    }
    
    public Set<Issue> getAssignedIssues() {
        return assignedIssues;
    }
    
    public void setAssignedIssues(Set<Issue> assignedIssues) {
        this.assignedIssues = assignedIssues;
    }
    
    public Set<Comment> getComments() {
        return comments;
    }
    
    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }
    
    public boolean hasPermission(Permission permission) {
        return role != null && role.getPermissions().contains(permission);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(getId(), user.getId()) && 
               Objects.equals(username, user.username) && 
               Objects.equals(email, user.email);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getId(), username, email);
    }
} 