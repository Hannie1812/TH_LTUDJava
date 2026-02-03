# TODO: Add Quantity Column for Books and Admin Alerts

## Step 1: Add Quantity Field to Book Entity
- Add `quantity` field to `Book.java` entity with appropriate annotations.

## Step 2: Update BookService
- Modify `BookService.java` to handle quantity updates.

## Step 3: Update BookController
- Modify `BookController.java` to include quantity in add/edit forms.

## Step 4: Update Templates
- Update `add.html` and `edit.html` to include quantity input field.
- Update `list.html` to display quantity column.

## Step 5: Update View Models
- Update `BookGetVm.java` and `BookPostVm.java` to include quantity.

## Step 6: Modify CartService for Quantity Deduction
- Update `CartService.java` to deduct quantity from book stock on checkout.

## Step 7: Add Admin Alert on Login
- Modify `HomeController.java` or create a service to check low stock and alert admin on login.
- Update `index.html` to display alerts.

## Step 8: Update Repository if Needed
- Check if `IBookRepository.java` needs updates for quantity queries.
