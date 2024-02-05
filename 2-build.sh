mkdir deploy
(cd projects/spring-app; gradle build)
cp projects/spring-app/build/libs/demo-0.0.1-SNAPSHOT.jar ./deploy/