# Instrumentation Testing with Dagger, Mockito, and Espresso

Historically, testing on Android has not been easy, but many awesome projects have emerged which, in combination, have made testing much simpler. We have found that a great combination of coffee, booze, and weaponry that eases this pain. Specifically, [Espresso](https://code.google.com/p/android-test-kit/), [Mockito](https://github.com/mockito/mockito), and [Dagger 2](http://google.github.io/dagger/).

## Dependency Injection
[Dependency injection](http://en.wikipedia.org/wiki/Dependency_injection) is the cornerstone of what makes much of the Circle Android app testable. Unfortunately, many Android framework classes (e.g. Activity) are not instantiated in your code. This makes it difficult to supply dependencies via  constructors (e.g. an instance of an API class to your Activity) and presents a real challenge when trying to write functional tests.

#### Dagger 2
There are a number of dependency injection libraries available for Java, but there is really only one worth considering for Android. **Dagger 2**. Dagger 2 performs depenency injection at compile time which means: 

* Injections will fail at compile time instead run time which lets your catch mistakes more quickly.
* There is no performance hit because injections are not performed using relfection like other dependency injection frameworks.
* No more proguard exceptions for injected code which both saves the developer time (and tears) and increases security.

Because Dagger 2's API and motiviation is very similar to the original Dagger and there are already several great introductions to Dagger, we're going just jump right into the code. I highly recommend the following resources for the uninitiated.

* [Greg Kick's Dagger 2 Presentation](https://www.youtube.com/watch?v=oK_XtfXPkqw) 
* [Jake Wharton's Dagger 2 Presentation](https://speakerdeck.com/jakewharton/dependency-injection-with-dagger-2-devoxx-2014)

*Note: Although Dagger 2 is technically [considered pre-alpha](https://github.com/google/dagger#status) by it's creators, it is actually being [used in critical production systems](https://github.com/google/dagger/issues/70) and the pre-alpha status is because there may be some small API changes to come.*

## A Simple Testable App
In our simple app, there is an activity that allows a user to authenticate some credentials. In our tests, we will verify that the activity under test behaves correctly depending on the API response returned. If the API confirms that the credentials are valid, the activity should launch a new activity. If the credentials are invalid, the activity should show an error message.

However, we want to just test the activity's behavior and not perform an end to end test. We don't actually want to make API calls because they slow down tests and we are only concerned that the activity's implementation is correct. By using mocks and dependency injection, we can write better tests more easily and without having to create weird accessor methods in our activities to support testing.

#### Setting up Dagger
Your `build.gradle` file will look sometihng like this.

In it you'll notice:

* We add the Sonatype Snapshot Respository to import our Dagger 2 dependencies since it has not yet been published to Maven Central. Also we import a gradle plugin called [Android-Apt](https://bitbucket.org/hvisser/android-apt) which helps Android Studio detect the classes Dagger 2 generates.
* We import Mockito and some libraries to make it play nice with Android's dexing. We import these only into our debug build, because the debug build is the default build that instrumentation tests are run against.
* And lastly, we import Espresso 2 from the Android Support Library (make sure you have updated your local Android Support repository from the SDK manager).

```gradle
buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        // This plugin helps Android Studio find Dagger's generated classes
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.4'
    }
}
apply plugin: 'com.android.application'
// apply the Android-Apt plugin to our project
apply plugin: 'com.neenbedankt.android-apt'

repositories {
    // Currently, Dagger 2 is published to Sonatype's maven repo
    maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
}

android {
    compileSdkVersion 22
    buildToolsVersion '21.1.2'

    defaultConfig {
        minSdkVersion 16
        targetSdkVersion 22
        versionCode 1
        versionName '1.0.0'
        applicationId 'com.circle.sample'
    }
}

dependencies {
    // Dagger 2 dependencies
    compile 'com.google.dagger:dagger:2.0-SNAPSHOT'
    apt 'com.google.dagger:dagger-compiler:2.0-SNAPSHOT'
    provided 'org.glassfish:javax.annotation:10.0-b28' // adds the @Generated annoation that Android lacks

    // Mockito Dependencies
    debugCompile 'com.google.dexmaker:dexmaker-mockito:1.0'
    debugCompile 'com.google.dexmaker:dexmaker:1.0'
    debugCompile 'org.mockito:mockito-core:1.10.17'

    // Espresso 2 Dependencies
    androidTestCompile 'com.android.support.test.espresso:espresso-core:2.0'
    androidTestCompile 'com.android.support.test:testing-support-lib:0.1'
}

```

#### Defining our Dependency Graphs
In our example application, we have an `Api` class that we want to inject into our Activities. For the sake of a simple example, our `Api` pretends to make a network call, but we will actually mock out this code later when testing.

```java
public class Api {

    public Observable<Boolean> login(String username, String password) {
        // imagine an actual Api call here
        return Observable.just(true);
    }
}
```

##### Modules
Modules are classes that define a set of providers (methods annotated with `@Provides`) and the dependency graph is created by a Component and the set of Modules it includes. Here are two very simple Modules which we will use in the project.

`DataModule` provides a single instance of our Api class. We will use this when compiling our release build variant.
```java
@Module
public final class DataModule {

    @Provides @Singleton
    public Api provideApi() {
        return new Api();
    }

}
```

`DebugDataModule` is similar to `DataModule`, but it can be initialized to return a mock of the Api class that we can use for testing. It is especially important that we use the `@Singleton` annotation on the provides method because it ensures that our test class and the class being tested receive the same variable instance. This module is used in our debug build variant.
```java
@Module
public final class DebugDataModule {

    private final boolean mockMode;

    public DebugDataModule(boolean provideMocks) {
        mockMode = provideMocks;
    }

    @Provides @Singleton
    Api provideApi() {
        if (mockMode) {
            return Mockito.mock(Api.class);
        } else {
            return new Api();
        }
    }

}
```

*If you are coming from Dagger 1, the modules are now simpler. Modules no longer need to define injectable classes or the modules they include. This is done by Components.*

##### Components
Component interfaces define the collection of modules that will be used to construct the dependency graph and the classes it can inject dependencies into.

The Dagger compiler will generate a class `Dagger_<ComponentName>` (e.g. `Dagger_Graph` is generated from our interface `Graph`). `Dagger_Graph` is a class that contains the full dependency graph you have defined and knows how to inject into the classes we've declared `injects()` methods for.

The release variant
```java
@Singleton
@Component(modules = {DataModule.class})
public interface Graph {

    void inject(BaseActivity activity);

    public final static class Initializer {
        public static Graph init(boolean mockMode) {
            return Dagger_Graph.builder()
                    .build();
        }
    }
}
```

The debug variant
```java
@Singleton
@Component(modules = {DebugDataModule.class})
public interface Graph {

    void inject(BaseActivity activity);
    void inject(InjectedActivityTest test);

    public final static class Initializer {
        public static Graph init(boolean mockMode) {
            return Dagger_Graph.builder()
                    .debugDataModule(new DebugDataModule(mockMode))
                    .build();
        }
    }
}
```

To create an instance of your graph, the generated class has a builder. You'll notice in the release variant that we do not pass in an instance of the `DataModule`, but in the debug variant we do. The builder will initilize the modules it includes if the module's constructor does not require any parameters. Otherwise, the builder generates methods matching its modules and
you must pass an instance in yourself.

The key difference to notice here is that release variant uses `DataModule` and the debug variant uses the `DebugDataModule`. The `DebugDataModule` will act normally unless when we initialize it, we put it in mock mode.

#### Injecting Dependencies
We create instances of our graph when start the app and access it from the Application and access it from classes though our `App`'s static accessor. e.g. `App.getInstance().graph()`.

```java
public class App extends Application {

    private static App sInstance;
    private Graph graph;

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;
        graph = Graph.Initializer.init(false);
    }

    public static App getInstance() {
        return sInstance;
    }

    public Graph graph() {
        return graph;
    }

    public void setMockMode(boolean useMock) {
        graph = Graph.Initializer.init(useMock);
    }

}
```

In our example we have a `BaseActivity` that all other activities inherit from, and this is where we will do the injection.

```java
public abstract class BaseActivity extends Activity {
    @Inject
    Api mApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.getInstance().graph().inject(this);
    }

    protected Api getApi() {
        return mApi;
    }
}
```

#### Writing Activity Tests
The final piece of the puzzle is to define the behavior of our mocks in our tests. Below is a rule you can use in your JUnit tests to fetch the dependency graph from your `Application` and make all the dependencies available to your testing code.

```java
public class InjectRule implements TestRule {

    DependencyWrapper dependencyWrapper = new DependencyWrapper();

    public InjectRule() {
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                App app = (App) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
                app.setMockMode(true);
                app.graph().inject(dependencyWrapper);

                base.evaluate();
            }
        };
    }
}
```

```java
public class DependencyWrapper {
    @Inject
    public Api api;
}
```

We recommend [Espresso](https://code.google.com/p/android-test-kit/) for writing UI tests, but you can use whatever you like. Espresso has a concise API and handles waits, sleeps, syncs, and polls. This greatly speeds up test writing and running.

So when we test an activity, we just define the mocking behavior that we want from the dependency we share with our Activity under test. Then confirm that the activity behaved this way. In this example, we have an [Activity](https://github.com/bryanstern/dagger-instrumentation-example/blob/master/app/src/main/java/com/circle/testexample/ui/MainActivity.java) that has two text fields that performs a log in request. We want to test that it starts the right activity with a successful response or shows an error message if it fails.

```java
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    // see https://gist.github.com/JakeWharton/1c2f2cadab2ddd97f9fb
    public final ActivityRule<MainActivity> main = new ActivityRule<>(MainActivity.class);
    public final InjectRule injectRule = new InjectRule();

    @Rule
    public final TestRule rule = RuleChain.outerRule(injectRule).around(main);

    @Test
    public void testLoginSuccess() {
        when(injectRule.dependencyWrapper.api.login("real@user.com", "secret")).thenReturn(Observable.just(true));

        ActivityMonitor monitor = main.instrumentation().addMonitor(AccountActivity.class.getName(), null, true);

        onView(withId(R.id.username)).perform(typeText("real@user.com"));
        onView(withId(R.id.password)).perform(typeText("secret"));
        onView(withId(R.id.button)).perform(click());

        assertEquals(1, monitor.getHits());

        main.instrumentation().removeMonitor(monitor);
    }

    @Test
    public void testLoginFailure() {
        when(injectRule.dependencyWrapper.api.login("real@user.com", "secret")).thenReturn(Observable.just(false));

        onView(withId(R.id.username)).perform(typeText("real@user.com"));
        onView(withId(R.id.password)).perform(typeText("secret"));
        onView(withId(R.id.button)).perform(click());

        onView(withText(main.get().getString(R.string.error_invalid_credentials))).check(matches(isDisplayed()));
    }
}

```

## Summary
If you want to see what this looks like altogether, check out our [sample project](https://github.com/bryanstern/dagger-instrumentation-example). To run the tests, start up an emulator and run `./gradlew cAT` from your terminal.Hopefully this will get you on your way to integrating the new, awesome version of Dagger and start writing more tests for your projects.

This is just one use of dependency injection, but [Jake Wharton's u2020 project](https://github.com/JakeWharton/u2020/) shows off a lot of the great uses for Dagger in your project and it has been a great resource for us.

Cheers!
