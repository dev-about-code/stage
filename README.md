# Stage framework
## Overview
Stage is a small and fast, no-fuzz and no-magic framework for developing business-grade Java applications.

It provides you with a lot of functionality that allows you to create powerful Java applications fast. 
However, removing boilerplating or letting you write code as fast as possible is not its main purpose. 
Instead, it allows you to focus on proper separation of concerns and helps you to keep you code-base 
structured and readable. This makes it the ideal candidate for applications that are expected to grow.

## Releases
### `3.0.6`
- Includes authorization checks in exception handling and allows custom exceptions to be thrown 
during auth
- `Version` now also allows incomplete version strings (e.g. `3.2` or even just `3`) 

### `3.0.5`
- Allows default values for `@QueryParameter` annotations
- `@QueryParameter` fields can also be enums now
- Empty values for non-mandatory parameters work now
- `Accept` header parsing is working for simple headers now
- `NonAuthorized` is now thrown as a `UnauthorizedException` and can hence be transformed to a custom response 

## Architecture
All functional units in Stage are called `Components`: small, self-contained pieces of code that provide 
very clearly defined functionality. `Components` have a lifecycle and can interact with other `Components` 
through a dependency mechanism. In this, they are very similar to Sprint Boot's Controllers. 

However, the major difference between Spring Boot and Stage is that the latter does not rely on automatic resolving
of dependencies, does not inject code anywhere and does not require XML or JSON configuration files 
to run. 
We think that configuration is code, hence all configuration in Stage is done in-code. This makes
the code much more readable and much easier to understand for developers that are new to the project. 

The main entry point for Stage is an instance of `Application` along with its `ApplicationContainer`:

```java
public class Main {
   public static void main(String[] args){
     ApplicationContainer.start(new Application() {
        @Override
        public void assemble(ApplicationAssemblyContext context) {
           // add your components here
        }
     }, args);
   }
}
```

Applications can be started to run once and then terminate (as in the example above) or to run until
the application is terminated explicitly.

TO BE CONTINUED
