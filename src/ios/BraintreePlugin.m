//
//  BraintreePlugin.m
//
//  Copyright (c) 2016 Justin Unterreiner. All rights reserved.
//

#import "BraintreePlugin.h"
#import <objc/runtime.h>
#import <BraintreeDropIn/BraintreeDropIn.h>
#import <BraintreeDropIn/BTDropInController.h>
#import <BraintreeCore/BTAPIClient.h>
#import <BraintreeCore/BTPaymentMethodNonce.h>
#import <BraintreeCard/BTCardNonce.h>
#import <BraintreePayPal/BraintreePayPal.h>
#import <BraintreeApplePay/BraintreeApplePay.h>
#import <Braintree3DSecure/Braintree3DSecure.h>
#import <BraintreeVenmo/BraintreeVenmo.h>
#import "AppDelegate.h"

@interface BraintreePlugin()

@property (nonatomic, strong) BTAPIClient *braintreeClient;
@property NSString* token;

@end

@implementation AppDelegate(BraintreePlugin)


- (BOOL)application:(UIApplication *)application
            openURL:(NSURL *)url
  sourceApplication:(NSString *)sourceApplication
         annotation:(id)annotation {
    NSString *bundle_id = [NSBundle mainBundle].bundleIdentifier;
    bundle_id = [bundle_id stringByAppendingString:@".payments"];
    
    if ([url.scheme localizedCaseInsensitiveCompare:bundle_id] == NSOrderedSame) {
        return [BTAppSwitch handleOpenURL:url sourceApplication:sourceApplication];
    }

    // all plugins will get the notification, and their handlers will be called
    [[NSNotificationCenter defaultCenter] postNotification:[NSNotification notificationWithName:CDVPluginHandleOpenURLNotification object:url]];

    return NO;
}

@end

@implementation BraintreePlugin

NSString *dropInUIcallbackId;

#pragma mark - Cordova commands

- (void)initialize:(CDVInvokedUrlCommand *)command {

    // Ensure we have the correct number of arguments.
    if ([command.arguments count] != 1) {
        CDVPluginResult *res = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"A token is required."];
        [self.commandDelegate sendPluginResult:res callbackId:command.callbackId];
        return;
    }

    // Obtain the arguments.
    self.token = [command.arguments objectAtIndex:0];

    if (!self.token) {
        CDVPluginResult *res = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"A token is required."];
        [self.commandDelegate sendPluginResult:res callbackId:command.callbackId];
        return;
    }

    self.braintreeClient = [[BTAPIClient alloc] initWithAuthorization:self.token];

    if (!self.braintreeClient) {
        CDVPluginResult *res = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"The Braintree client failed to initialize."];
        [self.commandDelegate sendPluginResult:res callbackId:command.callbackId];
        return;
    }

    NSString *bundle_id = [NSBundle mainBundle].bundleIdentifier;
    bundle_id = [bundle_id stringByAppendingString:@".payments"];

    [BTAppSwitch setReturnURLScheme:bundle_id];

    CDVPluginResult *res = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:res callbackId:command.callbackId];
}

- (void)presentDropInPaymentUI:(CDVInvokedUrlCommand *)command {

    // Ensure the client has been initialized.
    if (!self.braintreeClient) {
        CDVPluginResult *res = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"The Braintree client must first be initialized via BraintreePlugin.initialize(token)"];
        [self.commandDelegate sendPluginResult:res callbackId:command.callbackId];
        return;
    }

    // Ensure we have the correct number of arguments.
    if ([command.arguments count] != 6) {
        CDVPluginResult *res = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"cancelText, ctaText, title, amount, primaryDescription, and secondaryDescription are required."];
        [self.commandDelegate sendPluginResult:res callbackId:command.callbackId];
        return;
    }

    // Obtain the arguments.

    NSString* amount = [command.arguments objectAtIndex:0];
    
    if (!amount) {
        CDVPluginResult *res = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"amount is required."];
        [self.commandDelegate sendPluginResult:res callbackId:command.callbackId];
        return;
    }

    NSString* cancelText = [command.arguments objectAtIndex:1];

    NSString* title = [command.arguments objectAtIndex:2];

    NSString* ctaText = [command.arguments objectAtIndex:3];
    
    NSString* primaryDescription = [command.arguments objectAtIndex:4];
    
    NSString* secondaryDescription = [command.arguments objectAtIndex:5];
    
    // Save off the Cordova callback ID so it can be used in the completion handlers.
    dropInUIcallbackId = command.callbackId;

    /* Drop-IN 5.0 */
    BTDropInRequest *paymentRequest = [[BTDropInRequest alloc] init];
    paymentRequest.amount = amount;
    BTDropInController *dropIn = [[BTDropInController alloc] initWithAuthorization:self.token request:paymentRequest handler:^(BTDropInController * _Nonnull controller, BTDropInResult * _Nullable result, NSError * _Nullable error) {
        if (error != nil) {
            NSLog(@"ERROR");
        } else if (result.cancelled) {
            [self.viewController dismissViewControllerAnimated:YES completion:nil];
            if (dropInUIcallbackId) {
                
                NSDictionary *dictionary = @{ @"userCancelled": @YES };
                
                CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                              messageAsDictionary:dictionary];
                
                [self.commandDelegate sendPluginResult:pluginResult callbackId:dropInUIcallbackId];
                dropInUIcallbackId = nil;
            }
        } else {
            [self.viewController dismissViewControllerAnimated:YES completion:nil];
            if (dropInUIcallbackId) {
                NSDictionary *dictionary = [self getPaymentUINonceResult:result.paymentMethod];
                
                CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                              messageAsDictionary:dictionary];
                
                [self.commandDelegate sendPluginResult:pluginResult callbackId:dropInUIcallbackId];
                dropInUIcallbackId = nil;
            }
        }
    }];

    [self.viewController presentViewController:dropIn animated:YES completion:nil];
}

#pragma mark - Event Handlers

- (void)userDidCancelPayment {

    [self.viewController dismissViewControllerAnimated:YES completion:nil];

    if (dropInUIcallbackId) {

        NSDictionary *dictionary = @{ @"userCancelled": @YES };

        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                      messageAsDictionary:dictionary];

        [self.commandDelegate sendPluginResult:pluginResult callbackId:dropInUIcallbackId];
        dropInUIcallbackId = nil;
    }
}

#pragma mark - BTDropInViewControllerDelegate Members

- (void)dropInViewController:(BTDropInController *)viewController
  didSucceedWithTokenization:(BTPaymentMethodNonce *)paymentMethodNonce {

    [self.viewController dismissViewControllerAnimated:YES completion:nil];

    if (dropInUIcallbackId) {

        NSDictionary *dictionary = [self getPaymentUINonceResult:paymentMethodNonce];

        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                      messageAsDictionary:dictionary];

        [self.commandDelegate sendPluginResult:pluginResult callbackId:dropInUIcallbackId];
        dropInUIcallbackId = nil;
    }
}

- (void)dropInViewControllerDidCancel:(__unused BTDropInController *)viewController {

    [self.viewController dismissViewControllerAnimated:YES completion:nil];

    if (dropInUIcallbackId) {

        NSDictionary *dictionary = @{ @"userCancelled": @YES };

        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                      messageAsDictionary:dictionary];

        [self.commandDelegate sendPluginResult:pluginResult callbackId:dropInUIcallbackId];
        dropInUIcallbackId = nil;
    }
}

#pragma mark - Helpers

/**
 * Helper used to return a dictionary of values from the given payment method nonce.
 * Handles several different types of nonces (eg for cards, Apple Pay, PayPal, etc).
 */
- (NSDictionary*)getPaymentUINonceResult:(BTPaymentMethodNonce *)paymentMethodNonce {

    BTCardNonce *cardNonce;
    BTPayPalAccountNonce *payPalAccountNonce;
    BTApplePayCardNonce *applePayCardNonce;
    BTThreeDSecureCardNonce *threeDSecureCardNonce;
    BTVenmoAccountNonce *venmoAccountNonce;

    if ([paymentMethodNonce isKindOfClass:[BTCardNonce class]]) {
        cardNonce = (BTCardNonce*)paymentMethodNonce;
    }

    if ([paymentMethodNonce isKindOfClass:[BTPayPalAccountNonce class]]) {
        payPalAccountNonce = (BTPayPalAccountNonce*)paymentMethodNonce;
    }

    if ([paymentMethodNonce isKindOfClass:[BTApplePayCardNonce class]]) {
        applePayCardNonce = (BTApplePayCardNonce*)paymentMethodNonce;
    }

    if ([paymentMethodNonce isKindOfClass:[BTThreeDSecureCardNonce class]]) {
        threeDSecureCardNonce = (BTThreeDSecureCardNonce*)paymentMethodNonce;
    }

    if ([paymentMethodNonce isKindOfClass:[BTVenmoAccountNonce class]]) {
        venmoAccountNonce = (BTVenmoAccountNonce*)paymentMethodNonce;
    }

    NSDictionary *dictionary = @{ @"userCancelled": @NO,

                                  // Standard Fields
                                  @"nonce": paymentMethodNonce.nonce,
                                  @"type": paymentMethodNonce.type,
                                  @"localizedDescription": paymentMethodNonce.localizedDescription,

                                  // BTCardNonce Fields
                                  @"card": !cardNonce ? [NSNull null] : @{
                                          @"lastTwo": cardNonce.lastTwo,
                                          @"network": [self formatCardNetwork:cardNonce.cardNetwork]
                                          },

                                  // BTPayPalAccountNonce
                                  @"payPalAccount": !payPalAccountNonce ? [NSNull null] : @{
                                          @"email": payPalAccountNonce.email,
                                          @"firstName": (payPalAccountNonce.firstName == nil ? [NSNull null] : payPalAccountNonce.firstName),
                                          @"lastName": (payPalAccountNonce.lastName == nil ? [NSNull null] : payPalAccountNonce.lastName),
                                          @"phone": (payPalAccountNonce.phone == nil ? [NSNull null] : payPalAccountNonce.phone),
                                          //@"billingAddress" //TODO
                                          //@"shippingAddress" //TODO
                                          @"clientMetadataId":  (payPalAccountNonce.clientMetadataId == nil ? [NSNull null] : payPalAccountNonce.clientMetadataId),
                                          @"payerId": (payPalAccountNonce.payerId == nil ? [NSNull null] : payPalAccountNonce.payerId),
                                          },

                                  // BTApplePayCardNonce
                                  @"applePayCard": !applePayCardNonce ? [NSNull null] : @{
                                          },

                                  // BTThreeDSecureCardNonce Fields
                                  @"threeDSecureCard": !threeDSecureCardNonce ? [NSNull null] : @{
                                          @"liabilityShifted": threeDSecureCardNonce.liabilityShifted ? @YES : @NO,
                                          @"liabilityShiftPossible": threeDSecureCardNonce.liabilityShiftPossible ? @YES : @NO
                                          },

                                  // BTVenmoAccountNonce Fields
                                  @"venmoAccount": !venmoAccountNonce ? [NSNull null] : @{
                                          @"username": venmoAccountNonce.username
                                          }
                                  };
    return dictionary;
}

/**
 * Helper used to provide a string value for the given BTCardNetwork enumeration value.
 */
- (NSString*)formatCardNetwork:(BTCardNetwork)cardNetwork {
    NSString *result = nil;

    // TODO: This method should probably return the same values as the Android plugin for consistency.

    switch (cardNetwork) {
        case BTCardNetworkUnknown:
            result = @"BTCardNetworkUnknown";
            break;
        case BTCardNetworkAMEX:
            result = @"BTCardNetworkAMEX";
            break;
        case BTCardNetworkDinersClub:
            result = @"BTCardNetworkDinersClub";
            break;
        case BTCardNetworkDiscover:
            result = @"BTCardNetworkDiscover";
            break;
        case BTCardNetworkMasterCard:
            result = @"BTCardNetworkMasterCard";
            break;
        case BTCardNetworkVisa:
            result = @"BTCardNetworkVisa";
            break;
        case BTCardNetworkJCB:
            result = @"BTCardNetworkJCB";
            break;
        case BTCardNetworkLaser:
            result = @"BTCardNetworkLaser";
            break;
        case BTCardNetworkMaestro:
            result = @"BTCardNetworkMaestro";
            break;
        case BTCardNetworkUnionPay:
            result = @"BTCardNetworkUnionPay";
            break;
        case BTCardNetworkSolo:
            result = @"BTCardNetworkSolo";
            break;
        case BTCardNetworkSwitch:
            result = @"BTCardNetworkSwitch";
            break;
        case BTCardNetworkUKMaestro:
            result = @"BTCardNetworkUKMaestro";
            break;
        default:
            result = nil;
    }

    return result;
}

@end

