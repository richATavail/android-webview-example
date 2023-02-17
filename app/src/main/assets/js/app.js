/*
 * The total plain application.
 */

////////////////////////////////////////////////////////////////////////////////
//                             API Functions                                  //
////////////////////////////////////////////////////////////////////////////////

/**
 * Gets the text in the `toastMessage` component of internal.html.
 */
function getToastMessage() {
    console.error("This actually isn't an error, just an example!")
    return document.getElementById('toastMessage').value;
}

/**
 * Just concatenates two strings just because.
 *
 * @param arg1
 *   The string to be concatenated on.
 * @param arg2
 *   The string to append to arg1.
 */
function stringConcat(arg1, arg2) {
    console.log("Concatenating and this was logged to the log")
    return arg1 + arg2;
}

/**
 * Call the native Android API function, `displayToast`, to have it to show the
 * text from the `toastMessage` component of internal.html in a native Android
 * toast.
 */
function displayToast() {
    console.warn("Warning a Toast is about to happen!")
    // noinspection JSUnresolvedVariable
    ExampleNativeAndroidAPI.displayToast(getToastMessage());
}

/**
 * Gets the inputted strings to be concatenated and passes it to the Android
 * native API, `ExampleNativeAndroidAPI.roundTripConcat`.
 */
function concatPresentedStrings() {
    // noinspection JSUnresolvedVariable
    ExampleNativeAndroidAPI.roundTripConcat(
        document.getElementById('concat1').value,
        document.getElementById('concat2').value)
}

/**
 * Call the native Android API function, `displayToast`, to have it to show the
 * text from the `toastMessage` component of internal.html in a native Android
 * toast.
 */
function getFile() {
    console.log("Calling getFile")
    let convId = nextConversationId();
    let success = function (response) {
        let fileContent = document.getElementById("fileContent");
        let node = document.createTextNode(response.payload.fileText)
        fileContent.appendChild(node);
    };
    let failure = function (response) {
        console.error(response.payload.errorMessage)
    }
    registerConversation(convId, success, failure)
    // noinspection JSUnresolvedVariable
    ExampleNativeAndroidAPI.getFile(convId, "sample-file.txt");
}


////////////////////////////////////////////////////////////////////////////////
//                    Native Android APIMessage Handling                      //
////////////////////////////////////////////////////////////////////////////////

/**
 * The {@link JsonMessage#conversationId} generator for API requests to the
 * native Android API.
 * @type {number}
 */
let messageCounter = 0

/**
 * An object that is keyed by {@link JsonMessage#conversationId} to the
 * corresponding {@link AndroidAPICallback} responsible for handling the
 * response.
 * @type {{}}
 */
const conversations = {}

/**
 * Answer the next available {@link JsonMessage#conversationId} for a request
 * being made to the native Android API.
 * @returns {number}
 */
function nextConversationId () {
    return ++messageCounter;
}

/**
 * @param conversationId
 *   The id that uniquely identifies this conversation.
 * @param success
 *   The function that accepts an {@link APIJsonResponse} to call if the API
 *   request was successful.
 * @param failure
 *   The function that accepts an {@link APIJsonResponse} to call if the API
 *   request was unsuccessful.
 */
function registerConversation (conversationId, success, failure)
{
    conversations[conversationId] =
        new AndroidAPICallback(success, failure)
}

/**
 * Resolve the conversation for the given conversation id. If no conversation
 * @param conversationId
 *   The id that uniquely identifies the conversation to resolve.
 * @param message
 *   The string JSON of a {@link APIJsonResponse} to resolve the conversation.
 */
function resolveConversation (conversationId, message)
{
    if (!(conversationId in conversations)) return;
    let callback = conversations[conversationId];
    if (callback === undefined)
    {
        console.warn(
            `No conversation with id ${conversationId}. Received:\n`
            + JSON.stringify(message));
        return;
    }
    callback.process(message);

}

/**
 * A generic wrapper for a JSON message.
 */
class JsonMessage {
    /**
     * The positive integer that represents this instance of {@link JsonMessage}.
     * @type {number}
     */
    conversationId;

    /**
     * The type of message. This dictates the structure of the {@link payload}.
     * @type {string}
     */
    messageType;

    /**
     * The data contained in this message. This is an object and its contents
     * are dependent on the {@link messageType}.
     * @type {{}}
     */
    payload;

    constructor(conversationId, messageType, payload) {
        this.conversationId = conversationId
        this.messageType = messageType
        this.payload = payload
    }
}

/**
 * A {@link JsonMessage} that is a response to a native Android API request.
 */
class APIJsonResponse extends JsonMessage {

    /**
     * `true` if the response was successful; `false` otherwise.
     * @type {boolean}
     */
    succeeded;

    constructor(conversationId, messageType, succeeded, payload) {
        super(conversationId, messageType, payload)
        this.succeeded = succeeded
    }
}

/**
 * Holds the success/fail functions that handle the results of an asynchronous
 * call into the Android API.
 */
class AndroidAPICallback {

    /**
     * The function to call if the API request was successful.
     *
     * @param response
     *   The {@link APIJsonResponse} response to the Android API call.
     */
    success = function (response) {};

    /**
     * The function to call if the API request was unsuccessful.
     *
     * @param response
     *   The {@link APIJsonResponse} response to the Android API call.
     */
    failure = function (response) {};

    /**
     * @param success
     *   The function that accepts an {@link APIJsonResponse} to call if the API
     *   request was successful.
     * @param failure
     *   The function that accepts an {@link APIJsonResponse} to call if the API
     *   request was unsuccessful.
     */
    constructor(success, failure) {
        this.success = success
        this.failure = failure
    }

    /**
     * Process the received {@link APIJsonResponse}.
     * @param response
     *   The {@link APIJsonResponse} to process.
     */
    process(response) {
        if (response.succeeded) this.success(response)
        else this.failure(response)
    }
}

/**
 * A response acknowledging the action that needs no special action.
 */
class AcknowledgementResponse extends JsonMessage {
    /**
     * @param raw
     *   The raw JSON response.
     */
    constructor(raw) {
        super(
            raw.conversationId,
            "AcknowledgementResponse",
            raw.succeeded,
            raw.payload);
    }
}

/**
 * A response indicating an error occurred while handling the request.
 */
class ErrorResponse extends JsonMessage {
    /**
     * The error message that describes how the request failed.
     * @type {string}
     */
    errorMessage;

    /**
     * @param raw
     *   The raw JSON response.
     */
    constructor(raw) {
        super(
            raw.conversationId,
            "ErrorResponse",
            raw.succeeded,
            raw.payload);
        this.errorMessage = raw.payload.errorMessage
    }
}

/**
 * A response to getting the contents of a singular file.
 */
class GetFileResponse extends JsonMessage {
    /**
     * The text of the file.
     * @type {string}
     */
    fileText;

    /**
     * @param raw
     *   The raw JSON response.
     */
    constructor(raw) {
        super(
            raw.conversationId,
            "GetFileResponse",
            raw.succeeded,
            raw.payload);
        this.fileText = raw.payload.fileText
    }
}

/**
 * A response to getting the contents the application's files directory.
 */
class GetFileListResponse extends JsonMessage {
    /**
     * The array of pairs of path relative files and a boolean, true indicates
     * it is a directory or false if it is not a directory.
     * @type {[[]]}
     */
    files = [[]];

    /**
     * @param raw
     *   The raw JSON response.
     */
    constructor(raw) {
        super(
            raw.conversationId,
            "GetFileListResponse",
            raw.succeeded,
            raw.payload);
        this.files = raw.payload.files
    }
}