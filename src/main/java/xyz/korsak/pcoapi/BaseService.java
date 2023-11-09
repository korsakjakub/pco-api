package xyz.korsak.pcoapi;

import org.apache.commons.lang3.RandomStringUtils;

public abstract class BaseService {
    protected String generateRandomString(String prefix) {
        return prefix + "-" + RandomStringUtils.randomAlphanumeric(10);
    }
}
