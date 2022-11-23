package org.asamk.signal.manager.storage.recipients;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

import org.whispersystems.signalservice.api.push.PNI;
import org.whispersystems.signalservice.api.push.ServiceId;
import org.whispersystems.signalservice.api.push.SignalServiceAddress;

public class RecipientAddress {

    private final Optional<ServiceId> serviceId;
    private final Optional<PNI> pni;
    private final Optional<String> number;

    public RecipientAddress(@JsonProperty("serviceId") Optional<ServiceId> serviceId, @JsonProperty("pni") Optional<PNI> pni, @JsonProperty("number") Optional<String> number) {
        super();

        if (serviceId.isPresent() && serviceId.get().equals(ServiceId.UNKNOWN)) {
            serviceId = Optional.empty();
        }
        if (pni.isPresent() && pni.get().equals(ServiceId.UNKNOWN)) {
            pni = Optional.empty();
        }
        if (serviceId.isEmpty() && pni.isPresent()) {
            serviceId = Optional.of(pni.get());
        }
        if (serviceId.isEmpty() && number.isEmpty()) {
            throw new AssertionError("Must have either a ServiceId or E164 number!");
        }

        this.serviceId = serviceId;
        this.pni = pni;
        this.number = number;

    }

    public RecipientAddress(@JsonProperty("serviceId") Optional<ServiceId> serviceId, @JsonProperty("number") Optional<String> number) {
        this(serviceId, Optional.empty(), number);
    }

    public RecipientAddress(@JsonProperty("serviceId") ServiceId serviceId, @JsonProperty("e164") String e164) {
        this(Optional.ofNullable(serviceId), Optional.empty(), Optional.ofNullable(e164));
    }

    public RecipientAddress(@JsonProperty("serviceId") ServiceId serviceId, @JsonProperty("pni") PNI pni, @JsonProperty("e164") String e164) {
        this(Optional.ofNullable(serviceId), Optional.ofNullable(pni), Optional.ofNullable(e164));
    }

    public RecipientAddress(@JsonProperty("address") SignalServiceAddress address) {
        this(Optional.of(address.getServiceId()), Optional.empty(), address.getNumber());
    }

    public RecipientAddress(@JsonProperty("serviceId") ServiceId serviceId) {
        this(Optional.of(serviceId), Optional.empty());
    }

    public ServiceId getServiceId() {
        return serviceId.orElse(ServiceId.UNKNOWN);
    }

    public Optional<ServiceId> serviceId() {
        return serviceId;
    }

    public String getIdentifier() {
        if (serviceId.isPresent()) {
            return serviceId.get().toString();
        } else if (number.isPresent()) {
            return number.get();
        } else {
            throw new AssertionError("Given the checks in the constructor, this should not be possible.");
        }
    }

    public String getLegacyIdentifier() {
        if (number.isPresent()) {
            return number.get();
        } else if (serviceId.isPresent()) {
            return serviceId.get().toString();
        } else {
            throw new AssertionError("Given the checks in the constructor, this should not be possible.");
        }
    }

    public boolean matches(RecipientAddress other) {
        return (serviceId.isPresent() && other.serviceId.isPresent() && serviceId.get().equals(other.serviceId.get()))
                || (pni.isPresent() && other.serviceId.isPresent() && pni.get().equals(other.serviceId.get()))
                || (serviceId.isPresent() && other.pni.isPresent() && serviceId.get().equals(other.pni.get()))
                || (pni.isPresent() && other.pni.isPresent() && pni.get().equals(other.pni.get()))
                || (number.isPresent() && other.number.isPresent() && number.get().equals(other.number.get()));
    }

    public SignalServiceAddress toSignalServiceAddress() {
        return new SignalServiceAddress(getServiceId(), number);
    }

    public org.asamk.signal.manager.api.RecipientAddress toApiRecipientAddress() {
        return new org.asamk.signal.manager.api.RecipientAddress(serviceId.map(ServiceId::uuid), number);
    }

    public Optional<PNI> pni() {
        return pni;
    }

    public Optional<String> number() {
        return number;
    }
}
