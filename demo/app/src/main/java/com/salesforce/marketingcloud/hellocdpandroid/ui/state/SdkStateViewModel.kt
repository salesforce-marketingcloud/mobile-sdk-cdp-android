package com.salesforce.marketingcloud.hellocdpandroid.ui.state

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.salesforce.marketingcloud.cdp.CdpSdk
import com.salesforce.marketingcloud.cdp.consent.Consent
import com.salesforce.marketingcloud.cdp.location.Coordinates
import com.salesforce.marketingcloud.sfmcsdk.SFMCSdk
import com.salesforce.marketingcloud.sfmcsdk.components.events.*
import com.salesforce.marketingcloud.sfmcsdk.modules.ModuleIdentifier
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Sample Mobile Schema with custom attributes:
 * demo/app/schema/mobile-cdp-sdk-demo.json
 */
class SdkStateViewModel : ViewModel() {

    private val _message = MutableLiveData<String>().apply {
        value = ""
    }
    val message: LiveData<String> = _message

    private val _text = MutableLiveData<String>().apply {
        SFMCSdk.requestSdk {
            value = it.getSdkState().toString(4)
        }
    }
    val text: LiveData<String> = _text

    private val _consent = MutableLiveData<Boolean>().apply {
        CdpSdk.requestSdk { cdp ->
            value = cdp.consent == Consent.OPT_IN
        }
    }
    val consent: LiveData<Boolean> = _consent

    fun clearMessage() {
        _message.value = ""
    }

    /**
     * Consent Management
     * https://developer.salesforce.com/docs/atlas.en-us.c360a_api.meta/c360a_api/c360a_api_engagement_mobile_sdk_consent_management.htm
     *
     * Consent Schema
     * https://developer.salesforce.com/docs/atlas.en-us.236.0.c360a_api.meta/c360a_api/c360a_api_engagement_mobile_sdk_consent_schema.htm
     */
    fun toggleConsent(granted: Boolean) {
        CdpSdk.requestSdk { cdp ->
            // Set Consent
            cdp.consent = if (granted) Consent.OPT_IN else Consent.OPT_OUT

            // Update View, Message User
            _consent.value = cdp.consent == Consent.OPT_IN
            refreshStateWithMessage("Consent toggled: ${cdp.consent}")
        }
    }

    /**
     * Location Tracking
     * https://developer.salesforce.com/docs/atlas.en-us.c360a_api.meta/c360a_api/c360a_api_engagement_mobile_sdk_location_tracking.htm
     */
    fun setLocation() {
        CdpSdk.requestSdk { cdp ->
            // prepare the coordinates, use the Coordinates wrapper
            val coordinates = Coordinates(latitude = 54.187738, longitude = 15.554440)

            // set the location coordinates and expiration time in seconds
            cdp.setLocation(coordinates = coordinates, expiresIn = 300 /* 5 min */)

            refreshStateWithMessage("Location Set: $coordinates")
        }
    }

    /**
     * Capturing Engagement Data
     * https://developer.salesforce.com/docs/atlas.en-us.c360a_api.meta/c360a_api/c360a_api_engagement_mobile_sdk_capturing_engagement_data.htm
     */
    fun trackSampleEngagementEvents() {
        trackCartEvents()
        trackCatalogEvents()
        trackOrderEvents()
        trackCustomEvent()
    }

    /**
     * Cart (Engagement) Event
     * https://developer.salesforce.com/docs/atlas.en-us.236.0.c360a_api.meta/c360a_api/c360a_api_engagement_mobile_sdk_cart_event.htm
     *
     * Cart Schema
     * https://developer.salesforce.com/docs/atlas.en-us.236.0.c360a_api.meta/c360a_api/c360a_api_engagement_mobile_sdk_cart_schema.htm
     */
    fun trackCartEvents() {
        val event = CartEvent.add(
            lineItem = LineItem(
                catalogObjectId = "product-1",
                catalogObjectType = "Product",
                quantity = 1,
                price = 20.0,
                currency = "USD",
                attributes = mapOf(
                    "gift_wrap_color" to "red" // to be sent as attributeGiftWrapColor
                )
            )
        )
        event?.track()
        refreshStateWithMessage("Cart event tracked: ${event?.attributes()}")
    }

    /**
     * Catalog (Engagement) Event
     * https://developer.salesforce.com/docs/atlas.en-us.236.0.c360a_api.meta/c360a_api/c360a_api_engagement_mobile_sdk_catalog_event.htm
     *
     * Catalog Schema
     * https://developer.salesforce.com/docs/atlas.en-us.236.0.c360a_api.meta/c360a_api/c360a_api_engagement_mobile_sdk_catalog_schema.htm
     */
    fun trackCatalogEvents() {
        val event = CatalogEvent.view(
            CatalogObject(
                id = "product-1",
                type = "Product",
                attributes = mapOf(
                    "PROMO_CODE" to "FALL2021" // to be sent as attributePromoCode
                ),
                relatedCatalogObjects = mapOf(
                    "product's size" to listOf(
                        "S", // to be sent as relatedCatalogObjectProductsSize0
                        "M", // to be sent as relatedCatalogObjectProductsSize1
                        "L" // to be sent as relatedCatalogObjectProductsSize2
                    ),
                    "product-sku" to listOf(
                        "1234", // to be sent as relatedCatalogObjectProductSku0
                        "5678" // to be sent as relatedCatalogObjectProductSku1
                    )
                )
            )
        )
        event?.track()
        refreshStateWithMessage("Catalog event tracked: ${event?.attributes()}")
    }

    /**
     * Order (Engagement) Event
     * https://developer.salesforce.com/docs/atlas.en-us.236.0.c360a_api.meta/c360a_api/c360a_api_engagement_mobile_sdk_order_event.htm
     *
     * Order Schema
     * https://developer.salesforce.com/docs/atlas.en-us.236.0.c360a_api.meta/c360a_api/c360a_api_engagement_mobile_sdk_order_schema.htm
     */
    fun trackOrderEvents() {
        val event = OrderEvent.purchase(
            Order(
                id = "order-1",
                totalValue = 30.00,
                currency = "USD",
                attributes = mapOf(
                    "PROMO_CODE" to "HELLO" // to be sent as attributePromoCode
                ),
                lineItems = listOf(
                    LineItem(
                        catalogObjectId = "product-1",
                        catalogObjectType = "Product",
                        quantity = 1,
                        price = 20.00,
                        currency = "USD",
                        attributes = mapOf(
                            "gift_wrap" to "true", // to be sent as attributeGiftWrap, Booleans must be sent as Text or Number to match schema dataType
                            "gift card" to "Dearly Beloved" // to be sent as attributeGiftCard
                        )
                    ),
                    LineItem(
                        catalogObjectId = "product-2",
                        catalogObjectType = "Product",
                        quantity = 2,
                        price = 5.00,
                        currency = "USD",
                        attributes = mapOf(
                            "giftWrapColor" to "blue" // to be sent as attributeGiftWrapColor
                        )
                    )
                )
            )
        )
        event?.track()
        refreshStateWithMessage("Order event tracked: ${event?.attributes()}")
    }

    /**
     * Custom (Engagement) Event
     * https://developer.salesforce.com/docs/atlas.en-us.236.0.c360a_api.meta/c360a_api/c360a_api_engagement_mobile_sdk_custom_event.htm
     *
     * Custom Event Schema
     * https://developer.salesforce.com/docs/atlas.en-us.236.0.c360a_api.meta/c360a_api/c360a_api_engagement_mobile_sdk_custom_event_schema.htm
     *
     * The CDP SDK removes '` characters from attribute keys and splits words on spaces, underscores, and dots to convert to camelCase.
     *
     * For example:
     *      attributes = mapOf("That's o.k." to "true")
     *
     * Would align with schema:
     *      "masterLabel": "That's o.k.",
     *      "developerName": "thatsOK",
     *      "dataType": "Text"
     *
     * For any of the pre-defined structured events (Cart, Catalog, Order),
     * the lineItem.attributes, catalogObjectEvent.catalogObject.attributes, and orderEvent.order.attributes
     * are also prefixed with "attribute"; so all these examples would be converted to "attributePromoCode":
     *      "promoCode"
     *      "Promo code"
     *      "PROMO_CODE"
     */
    fun trackCustomEvent() {
        // It's good practise to Download / Preview your schema after Upload to verify the resulting developerNames that will be accepted on events / attributes.
        val event = EventManager.customEvent(
            name = "customGiftEvent", // try to avoid spaces in event names as they will be replaced with an underscore when uploaded in the schema.
            attributes = mapOf(
                "gift" to mapOf(
                    "message" to "Happy Birthday", // to be sent as giftMessage
                    "wrap" to "true", // to be sent as giftWrap, Booleans must be sent as Text or Number to match schema dataType
                    "wrapOptions" to mapOf(
                        "paper-color" to "green", // to be sent as giftWrapOptionsPaperColor
                        "ribbon" to 1 // to be sent as giftWrapOptionsRibbon, Booleans must be sent as Text or Number to match schema dataType
                    ),
                    "shipDate" to localDateTimeToUTC_ISO_8601(
                        LocalDateTime.of(2023, 4, 14, 16, 30
                    )) // to be sent as giftShipDate
                )
            )
        )
        event?.track()
        refreshStateWithMessage("Custom event tracked: ${event?.attributes()}")
    }

    private fun localDateTimeToUTC_ISO_8601(localDateTime: LocalDateTime): String? {
        val offsetDate = OffsetDateTime.of(localDateTime, ZoneOffset.UTC)
        return offsetDate.format(DateTimeFormatter.ISO_DATE_TIME)
    }

    /**
     * Capturing Profile Data
     * https://developer.salesforce.com/docs/atlas.en-us.c360a_api.meta/c360a_api/c360a_api_engagement_mobile_sdk_capturing_profile_data.htm
     *
     * Profile updates are recommended through the Identity API.
     * https://salesforce-marketingcloud.github.io/MarketingCloudSDK-Android/javadocs/SFMCSdk/8.0/com.salesforce.marketingcloud.sfmcsdk.components.identity/-identity/index.html
     *
     * This handles tracking our pre-defined Profile events,
     * and allows you to broadcast the attributes between modules.
     *
     * One limitation is that it only accepts String values.
     * Since the backend accepts dataTypes "Text", "DateTime", and "Number" (but not Boolean),
     * you can alternatively create & track Profile events as follows:
     *
     * val event = Event.profile("identity", mapOf("isAnonymous" to 1))
     * SFMCSdk.track(event)
     */
    fun trackSampleProfileEvents() {
        SFMCSdk.requestSdk { sdk ->
            sdk.identity.edit {
                attributes.putAll(
                sampleIdentity()
                        + sampleContactPointEmail()
                        + sampleContactPointPhone()
                        + sampleContactPointAddress()) }
            refreshStateWithMessage("Profile events tracked.")
        }

        // toggleAnonymous(true)
    }

    /**
     * Anonymous and Known Users
     * https://developer.salesforce.com/docs/atlas.en-us.c360a_api.meta/c360a_api/c360a_api_engagement_mobile_sdk_capturing_profile_data.htm
     *
     * Although we accept isAnonymous as 1, "1", true, "true", it is defined in the schema
     * with dataType "Number" since our backend does not currently support dataType "Boolean".
     * The SDK will attempt to convert any of these values to the Number 1, otherwise it
     * will default to 0 (even if it's excluded) since isDataRequired = true.
     *
     * This isn't the same for other attributes, so be mindful when using custom attributes
     * and isDataRequired as any events that do not match the schema definition will be ignored.
     */
    fun toggleAnonymous(anonymous: Boolean) {
        SFMCSdk.requestSdk { sdk ->
            val anonymousVal = if (anonymous) "1" else "0"
            sdk.identity.edit {
                attributes.put("isAnonymous", anonymousVal)
            }

            refreshStateWithMessage("identity isAnonymous: $anonymousVal")
        }
    }

    /**
     * Identity Schema
     * https://developer.salesforce.com/docs/atlas.en-us.c360a_api.meta/c360a_api/c360a_api_engagement_mobile_sdk_identity_schema.htm
     */
    fun sampleIdentity(): Map<String, String> {
        return mapOf(
            "firstName" to "First",
            "lastName" to "Last",
            "isAnonymous" to "0" // default "0", send "1" to track as anonymous.
        )
    }

    /**
     * Contact Point Email Schema
     * https://developer.salesforce.com/docs/atlas.en-us.c360a_api.meta/c360a_api/c360a_api_engagement_mobile_sdk_contact_point_email_schema.htm
     */
    fun sampleContactPointEmail(): Map<String, String> {
        return mapOf(
            "email" to "first.last@email.com"
        )
    }

    /**
     * Contact Point Phone Schema
     * https://developer.salesforce.com/docs/atlas.en-us.c360a_api.meta/c360a_api/c360a_api_engagement_mobile_sdk_contact_point_phone_schema.htm
     */
    fun sampleContactPointPhone(): Map<String, String> {
        return mapOf(
            "phoneNumber" to "1234567899"
        )
    }

    /**
     * Contact Point Address Schema
     * https://developer.salesforce.com/docs/atlas.en-us.c360a_api.meta/c360a_api/c360a_api_engagement_mobile_sdk_contact_point_address_schema.htm
     */
    fun sampleContactPointAddress(): Map<String, String> {
        return mapOf(
            "addressLine1" to "123 quality st",
            "addressLine2" to "", // optional
            "addressLine3" to "", // optional
            "addressLine4" to "", // optional
            "city" to "New York",
            "stateProvince" to "NY",
            "postalCode" to "10001",
            "country" to "US"
        )
    }

    /**
     * Marketing Cloud: Device and Contact Registration -> this will track a partyIdentification Profile event with CDP
     * https://salesforce-marketingcloud.github.io/MarketingCloudSDK-Android/sdk-implementation/device-contact-registration.html
     *
     * Party Identification Schema
     * https://developer.salesforce.com/docs/atlas.en-us.c360a_api.meta/c360a_api/c360a_api_engagement_mobile_sdk_party_identification_schema.htm
     */
    fun setContactKey(contactKey: String = UUID.randomUUID().toString()) {
        SFMCSdk.requestSdk { sdk ->
            sdk.identity.edit {
                profileId = contactKey
            } // PUSH contactKey/subscriberKey -> CDP userId

            refreshStateWithMessage("contactKey Set: $contactKey")
        }
    }

    // Update View
    private fun refreshStateWithMessage(message: String? = null) {
        message?.let { _message.value = message }

        SFMCSdk.requestSdk {
            _text.value = it.getSdkState().toString(4)
        }
    }
}
