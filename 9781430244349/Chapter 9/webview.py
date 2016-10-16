#
# SL4A WebView based UI
#
# @author Onur Cinar
#
import android

# Intialize the SL4A Android RCP Client
droid = android.Android()

# Show the HTML page
droid.webViewShow("file:///sdcard/sl4a/scripts/webview.html")

# We will calculate recursively
while True:
    # Wait for calculate event
    result = droid.eventWaitFor("calculate").result

    # Make sure that event has data
    if result is not None:
        # Data comes as a comma separated list of values
        request = result["data"].split(",")

        # Extract parameters from request array
        first = int(request[0])
        second = int(request[1])
        operation = request[2]

        # Calculate solution
        solution = {
            "+" : first + second,
            "-" : first - second,
            "*" : first * second,
            "/" : first / second
        }[operation]


        # Post the solution to event queue
        droid.eventPost("solution", str(solution))

