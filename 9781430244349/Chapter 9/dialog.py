#
# SL4A Dialog based UI
#
# @author Onur Cinar
#
import android

# Intialize the SL4A Android RCP Client
droid = android.Android()

# Title of our dialogs
title = "Calculator"

# We will calculate recursively
while True:
    # Get the first number from the user
    result = droid.dialogGetInput(title, "Enter the first number:").result
    
    # Check if user answered it
    if result is None:
        break
    
    # Convert the text input to an integer
    first = int(result)
    
    # Get the second number from the user
    result = droid.dialogGetInput(title, "Enter the second number:").result
    
    # Check if user answered it
    if result is None:
        break
    
    # Convert the text input to an integer
    second = int(result)
    
    # List of possible operations
    operations = [ "+", "-", "*", "/" ]
    
    # Open a generic dialog
    droid.dialogCreateInput(title, "Select operation")
    
    # Set the items to make it a list
    droid.dialogSetItems(operations)
    
    # Make the dialog visible
    droid.dialogShow()
    
    # Get the user's response
    result = droid.dialogGetResponse().result
    
    # Check if user answered it
    if (result is None) or (result.has_key("canceled")):
        break
    
    # Get the index of selected operation
    index = result["item"]
    
    # Find the operation at that index
    operation = operations[index]
    
    # Do the calculation
    solution = {
      "+" : first + second,
      "-" : first - second,
      "*" : first * second,
      "/" : first / second
    }[operation]
    
    # Show the solution and ask if user wants 
    # to do more calculations
    droid.dialogCreateAlert(title, "The solution is %d. New calculation?" % solution)
    
    # Set the answer options
    droid.dialogSetPositiveButtonText("Yes")
    droid.dialogSetNegativeButtonText("No")
    
    # Show dialog
    droid.dialogShow()
    
    # Get the user's response
    result = droid.dialogGetResponse().result

    # Check if user answered it
    if (result is None) or (result.has_key("canceled")):
        break
    
    # If user answer saying no
    option = result["which"]
    if option == "negative":
        break

# Terimnating script
droid.makeToast("Thank you")
