package disenodesistemas.backendfunerariaapp.buckets;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BucketName {

    ITEM_IMAGE("funerariadb-images");

    private final String name;
}
