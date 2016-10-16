#
# SL4A Full screen UI
#
# @author Onur Cinar
#
import android

# Intialize the SL4A Android RCP Client
droid = android.Android()

# XML layout
layout = """<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="fill_parent"
    android:layout_height="fill_parent"
    android:orientation="vertical"
    android:background="#ffffffff">

    <TextView
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:text="First number:" />
    
    <EditText        
        android:id="@+id/first"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:numeric="integer"
        android:inputType="number" />

    <TextView
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:text="Second number:" />
    
    <EditText        
        android:id="@+id/second"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:numeric="integer"
        android:inputType="number" />

    <TextView
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:text="Operation:" />

    <Spinner 
        android:id="@+id/operation"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content" />    
        
    <Button 
        android:id="@+id/calculate"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"    
        android:text="Calculate"    
        />
    
    <TextView
        android:id="@+id/solution"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content" />
</LinearLayout>
"""

# Show layout
droid.fullShow(layout)

# List of possible operations
operations = [ "+", "-", "*", "/" ]

# Set operation spinner items
droid.fullSetList("operation", operations)

# We will calculate recursively
while True:
    # Wait for calculate event
    event = droid.eventWaitFor("click").result

    # Check if it is the calculate button
    if event["data"]["id"] == "calculate":

        # Get the first number
        field = droid.fullQueryDetail("first").result["text"]

        # Check if field is empty
        if field == "":
            continue

        # Convert field to integer
        first = int(field)

        # Get the first number
        field = droid.fullQueryDetail("second").result["text"]

        # Check if field is empty
        if field == "":
            continue

        # Convert field to integer
        second = int(field)

        # Get the operation index
        index = int(droid.fullQueryDetail("operation").result["selectedItemPosition"])

        # Get operation
        operation = operations[index]

        # Do the calculation
        solution = {
          "+" : first + second,
          "-" : first - second,
          "*" : first * second,
          "/" : first / second
        }[operation]

        # Show solution
        droid.fullSetProperty("solution", "text", "Solution is %d" % solution)

