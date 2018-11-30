# Trusona OpenID Connect Example using Java and Spring Boot

This project demonstrates integrating Java and Trusona using Spring Boot.

Signup for a Trusona Developers account at https://developers.trusona.com/

## Requirements

* Trusona developer account
* Familiarity with the Spring Boot framework

## Project setup

1. Checkout the project from GitHub
1. Email `support@trusona.com` and let them know that you want to run this example. Include the email that is displayed when you log in to your Trusona Developers account.
1. You'll receive an email back with your OIDC client ID
1. Edit `src/main/resources/application.properties` enter your client ID for the key `oidc.clientId`
1. Run `./gradlew bootrun`

## Using the example

1. Open a browser and navigate to `https://localhost:5001`
1. Click the "Login with Trusona button" that you see on the screen
1. Open the Trusona App on your phone
1. Now that you've been redirect to the Trusona Gateway, use the Trusona App to scan the QR code on your screen
1. Tap "Accept" in the Trusona App
1. Now you've been redirected back to the example application running on your machine
1. See the user information displayed on the screen that indicates a successful login

## What's going on?

1. A request is prepared that will redirect the user to the Trusona Gateway and start the OIDC flow. (See the [OidcController.java](https://github.com/trusona/trusona-oidc-example-springboot/blob/2d287b5da4d5df7a80b26fd532eedceb9df8eec5/src/main/java/com/trusona/example/oidc/OidcController.java#L62))
1. The user clicks the login button which triggers the redirect to the Trusona Gateway. (See the [oidc.html template](https://github.com/trusona/trusona-oidc-example-springboot/blob/2d287b5da4d5df7a80b26fd532eedceb9df8eec5/src/main/resources/templates/oidc.html#L16))
1. Now the user is on the Trusona Gateway and is ready to complete their authentication
1. When the user returns to our example application with an ID token it is parsed and the claims information is displayed for your reference. (See the [OidcController.java](https://github.com/trusona/trusona-oidc-example-springboot/blob/2d287b5da4d5df7a80b26fd532eedceb9df8eec5/src/main/java/com/trusona/example/oidc/OidcController.java#L83) and the [success.html template](https://github.com/trusona/trusona-oidc-example-springboot/blob/2d287b5da4d5df7a80b26fd532eedceb9df8eec5/src/main/resources/templates/success.html#L10))