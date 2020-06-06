package com.polito.humantohuman.Database;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

public class MigrationDatabase implements RealmMigration {
    @Override
    public void migrate(final DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();
        if(oldVersion < 3) {
            RealmObjectSchema locSchema = schema.create("LocationConn")
                    .addField("latitude",double.class)
                    .addField("longitude",double.class)
                    .addField("time",long.class)
                    .addField("altitude",double.class)
                    .addField("accuracy",float.class);

            schema.get("ConnObject")
                    .addRealmObjectField("locationConn", locSchema)
                    .transform(new RealmObjectSchema.Function() {
                @Override
                public void apply(DynamicRealmObject obj) {
                    obj.set("locationConn", null);
                }
            });

        }
    }
}
