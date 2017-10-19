# android-falx
Android battery monitoring

## Realm notes:
Libraries that include Realm must expose and use their schema through a RealmModule. Doing so prevents the default RealmModule from being generated for the library project, which would conflict with the default RealmModule used by the app.

https://realm.io/docs/java/latest/#sharing-schemas

A RealmModule is added to the project on v0.1.4 to handle this issue.
