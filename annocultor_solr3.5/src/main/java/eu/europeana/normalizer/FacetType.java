package eu.europeana.normalizer;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public enum FacetType {
    LOCATION(false),
    SUBJECT(false),
    LANGUAGE(true),
    YEAR(true),
    TYPE(true),
    PROVIDER(true),
    COUNTRY(true),
    CONTRIBUTOR(false),
    USERTAGS(false),
    SOCIAL_TAGS(false),
    // todo maybe remove later. Now only used for ISTI AS
    TYPE_CONSTRAINTS(false);

    private boolean searchable;

    FacetType(boolean searchable) {
        this.searchable = searchable;
    }

    public boolean isSearchable() {
        return searchable;
    }
}