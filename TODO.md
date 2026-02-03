# TODO: Implement User Profile Management and Admin User Management

## Step 1: Update UserService.java
- [x] Add methods: updateUser, changePassword, findAllUsers, deleteUser, assignRole, removeRole.

## Step 2: Update IUserRepository.java
- [x] Add findAll method if not present (already extends JpaRepository).

## Step 3: Update UserController.java
- [x] Add /profile GET/POST endpoints for user profile management.
- [x] Add /admin/users GET endpoint for user list.
- [x] Add /admin/users/edit/{id} GET/POST for editing user.
- [x] Add /admin/users/delete/{id} POST for deleting user.
- [x] Add /admin/users/add GET/POST for creating user.

## Step 4: Update SecurityConfig.java
- [x] Add authorization for /profile (USER, ADMIN).
- [x] Add authorization for /admin/users/** (ADMIN).

## Step 5: Create user/profile.html template
- [x] Form for editing user info and changing password.

## Step 6: Create user/admin/list.html template
- [x] Table listing all users with edit/delete links.

## Step 7: Create user/admin/edit.html template
- [x] Form for editing user details and roles.

## Step 8: Create user/admin/add.html template
- [x] Form for adding new user.

## Step 9: Update layout.html
- [x] Add Profile link for logged-in users.
- [x] Add Admin Users link for ADMIN role.

## Step 10: Test the features
- Run application, test as USER and ADMIN.
