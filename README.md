# Trusona OpenID Connect Example using Java and Spring Boot

This project demonstrates integrating Java and Trusona using Spring Boot.

Signup for a Trusona Developers account at https://developers.trusona.com/

## Requirements

* Trusona Developers Account

## Running the Example

1. Checkout the project from Github
1. Email `support@trusona.com` and let them know that you want to run this example. Include the email that is displayed when you log in to your Trusona Developers account.
1. You'll receive an email back with your OIDC client ID
1. Run `export OIDC_CLIENT_ID="<Your client ID>"`
1. Run `./gradlew bootrun`
1. Open a browser and navigate to `https://localhost:5001`
1. Click the "Login with Trusona button" that you see on the screen
1. Open the Trusona App on your phone
1. Now that you've been redirect to the Trusona Gateway, use the Trusona App to scan the QR code on your screen
1. Tap "Accept" in the Trusona App
1. Now you've been redirected back to the example application running on your machine
1. See the user information displayed on the screen that indicates a successful login

## Technical Information


