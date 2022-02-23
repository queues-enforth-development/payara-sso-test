# payara-sso-test
Test suite for Payara SSO issue

GitHub payara / Payara Issue #5551

Deploy both AppA and AppB on Payara 5 2021/10 with SSO enabled.

Launch AppA

Login will be requested- you can fill in any user name and password that are the same, such as "dog" and "dog"

AppA's index page will be display

Type in a value for the name and press button- you will see message



Launch AppB

You will go directly to AppB's index page without a need for login

Type in a value for the name and press button- will will see message

Use the icon at the upper right of the top navigation bar to logout

Login screen is now displayed


Now- Lauch AppA again

You will go right to index page in AppA without being asked to login

Since we are using SSO, this should not happen
