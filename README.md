# Not KotDog

Not KotDog is an Android app written in Kotlin that uses the [Clarifai](http://clarifai.com) API to tell you whether or not an image is a hot dog.

## Setup

All you have to do to run this app is clone the repository, and add two string resources. You can either add these to `strings.xml`, or create a new file called `credentials.xml` which is what I've done to ensure that my strings won't appear in Git. The two resources are your Clarifai API ID and Secret:

```xml
<resources>
    <string name="api_id">AAAAAAID</string>
    <string name="api_secret">AAAAASECRET</string>
</resources>
```