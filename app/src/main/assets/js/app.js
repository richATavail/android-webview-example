/* jshint -W097 */
'use strict';
/*jshint esversion: 10 */
/* global console*/
/* global alert*/
/* class fields*/
/* global ExampleNativeAndroidAPI*/

/*
 * The total plain application.
 */

/**
 * The key used to serialize data and store in window local storage.
 * @type {string}
 */
const APP_STATE_STORAGE_KEY = "web-app-state";

/**
 * A context that manages this web apps state. It serves 2 main purposes:
 *
 * <ul>
 *     <li>
 *         Manage async message passing with Android API.
 *     </li>
 *     <li>
 *         Manage saving and restoring app state when Android lifecycle events
 *         require it.
 *     </li>
 * </ul>
 */
class AppDataContext {
	/**
	 * Construct the AppDataContext.
	 */
	constructor() {
		/**
		 * @property {number} myCounter
		 *   An example of state used in the app that has nothing to do with the
		 *   native Android.
		 */
		this.myCounter = 0;

		/**
		 * @property {number} messageCounter
		 *   The {@link AndroidAPIRequest#conversationId conversation id}
		 *   generator for {@link AndroidAPIRequest}s submitted to the native
		 *   Android API.
		 */
		this.messageCounter = 0;

		/**
		 * @property {Map<number, AndroidAPIRequest>} conversations
		 *   The map from the
		 *   {@link AndroidAPIRequest#conversationId conversation id} to the
		 *   corresponding {@link AndroidAPIRequest} awaiting a response.
		 */
		this.conversations = new Map();

		/**
		 * @property {boolean} isProcessingMessages
		 *   `true` indicates {@link AndroidAPIResponse message} processing is
		 *   allowed to proceed as the message is received; `false` indicates
		 *   message processing has been paused and received messages will be
		 *   sent to the {@link #messageQueue} to be processed later. The only
		 *   exception is {@link MessageType#CheckFreshStart}. This request is
		 *   used to initialize
		 */
		this.isProcessingMessages = false;

		/**
		 * @property {AndroidAPIResponse[]} messageQueue
		 *   The queue used to queue messages when message processing is
		 *   {@link #isProcessingMessages paused}.
		 */
		this.messageQueue = [];
	}

	/**
	 * Answer the next available {@link AndroidAPIRequest#conversationId} for a
	 * request being made to the native Android API.
	 * @returns {number}
	 */
	nextConversationId () {
		return ++this.messageCounter;
	}

	/**
	 * @param {AndroidAPIRequest} request
	 *   The function that accepts an {@link AndroidAPIResponse} to call if the API
	 *   request was successful.
	 */
	registerConversation (request) {
		this.conversations[request.conversationId] = request;
	}

	/**
	 * Process the provided {@link AndroidAPIResponse message}.
	 *
	 * Note: Unfortunately JSHint doesn't support a lot of modern class features,
	 * in this case, private methods.
	 *
	 * @param {AndroidAPIResponse} message
	 *   The message to process.
	 */
	processMessage (message) {
		try {
			let conversationId = message.conversationId;
			let request = this.conversations[conversationId];
			if (request === undefined) {
				console.warn(
					`No conversation with id ${conversationId}. Received:\n` +
					JSON.stringify(message));
				return;
			}
			console.log(
				`Processing response to ${request.messageType} (${conversationId})`);
			MessageType[message.messageType].process(message);
		}
		catch (e) {
			console.error(
				`Failed to process Android message:\n${e}\n${JSON.stringify(message)}`);
		}
	}

	/**
	 * Resolve the conversation for the given conversation id. If no conversation
	 * matching the conversation id exists, logs a warning and does nothing.
	 *
	 * @param message
	 *   The string JSON of a {@link AndroidAPIResponse} to resolve the conversation.
	 */
	resolveConversation (message) {
		if (!this.isProcessingMessages)
		{
			let processQueue = false;
			switch (message.messageType) {
				case MessageType.CheckFreshStart.name: {
					this.conversations.delete(message.conversationId);
					MessageType.CheckFreshStart.process(message);
					processQueue = true;
					break;
				}
				case MessageType.Error.name: {
					if (message.originatingMessageType === MessageType.Error.name) {
						// The CheckFreshStart had an error. Not much we can do
						// but presume there is something wrong with the native
						// Android environment. So, don't load data, just start.
						this.conversations.delete(message.conversationId);
						MessageType.Error.process(message);
						processQueue = true;
					}
					else {
						this.messageQueue.push(message);
					}
					break;
				}
				default: {
					this.messageQueue.push(message);
				}
			}
			if (processQueue) {
				this.isProcessingMessages = true;
				while (this.messageQueue.length > 0) {
					this.processMessage(this.messageQueue.shift());
				}
				// noinspection JSUnresolvedFunction
				ExampleNativeAndroidAPI.getAsyncResponses();
			}
			return;
		}
		this.processMessage(message);
	}

	// noinspection JSUnusedGlobalSymbols
	/**
	 * Customize how this {@link AppDataContext} is serialized as JSON.
	 *
	 * @returns {{}}
	 */
	toJSON() {
		return {
			myCounter: this.myCounter,
			messageCounter: this.messageCounter,
			conversations: this.conversations
		};
	}

	/**
	 * Save this {@link AppDataContext} to {@link localStorage}.
	 */
	saveState () {
		console.log("Saving state...");
		window.localStorage.setItem(APP_STATE_STORAGE_KEY, JSON.stringify(this));
	}

	/**
	 * Retrieve the {@link #saveState saved state} from {@link localStorage}
	 * and use it to populate this {@link AppDataContext}.
	 */
	restoreSavedState () {
		if (localStorage === null) { return; }
		let stored = localStorage.getItem(APP_STATE_STORAGE_KEY);

		if (stored === null) { return; }
		try {
			let data = JSON.parse(stored);
			this.myCounter = data.myCounter;
			this.messageCounter = data.messageCounter;
			this.conversations = data.conversations;
		}
		catch (e) {
			console.error(`Failed to parse saved AppDataContext state:\n ${e}`);
		}
		populateMyCounter();
	}
}

/**
 * The {@link AppDataContext} that manages the application state.
 * @type {AppDataContext}
 */
const context = new AppDataContext();

/**
 * Restore app data from {@link localStorage} if it exists.
 */
function restoreState () {
	context.restoreSavedState();
}

/**
 * Populates the onscreen HTML element, myCounter, with
 * {@link AppDataContext#myCounter}
 */
function populateMyCounter() {
	let myCounterElement = document.getElementById("myCounter");
	if (myCounterElement === null) { return; }
	let node = document.createTextNode(context.myCounter.toString());
	while (myCounterElement.firstChild) {
		myCounterElement.removeChild(myCounterElement.firstChild);
	}
	myCounterElement.appendChild(node);
}

/**
 * Increments {@link myCounter} then calls {@link populateMyCounter} to update
 * the value on screen.
 */
function incrementMyCounterAndPopulate () {
	context.myCounter++;
	populateMyCounter();
}

////////////////////////////////////////////////////////////////////////////////
//                           General Functions                                //
////////////////////////////////////////////////////////////////////////////////

// noinspection JSUnusedGlobalSymbols
/**
 * Resolve the conversation for the given conversation id. This function is
 * only called by the native Android API code to resolve an
 * {@link AndroidAPIRequest}.
 *
 * @param {{}} message
 *   The JSON of a {@link AndroidAPIResponse} to resolve the conversation.
 */
function resolveConversation (message)
{
	context.resolveConversation(message);
}

/**
 * Save the app state to {@link localStorage}.
 */
function saveState () {
	context.saveState();
}

/**
 * Clean the {@link saveState saved} application data from {@link localStorage}.
 */
function clearAppLocalStorage () {
	console.log("Web app clearing localStorage of app state...");
	if (localStorage !== null) {
		let retrieved = localStorage.getItem(APP_STATE_STORAGE_KEY);
		if (retrieved !== null) {
			console.log(`Cleaning state:\n\t${retrieved}`);
		}
		localStorage.removeItem(APP_STATE_STORAGE_KEY);
	}
}

/**
 * Gets the text in the `toastMessage` component of internal.html.
 */
function getToastMessage() {
	console.error("This actually isn't an error, just an example!");
	return document.getElementById('toastMessage').value;
}

// noinspection JSUnusedGlobalSymbols
/**
 * Just concatenates two strings just because.
 *
 * This code is only called from the native Android code to demonstrate features
 * associated with working with an Android WebView.
 *
 * @param arg1
 *   The string to be concatenated on.
 * @param arg2
 *   The string to append to arg1.
 */
function stringConcat(arg1, arg2) {
	console.log("Concatenating and this was logged to the log");

	// The returned string is passed back to the native Android code in the
	// callback provided to `WebView.evaluateJavascript`. See
	// ExampleJavascriptAPI.jsConcat to trace this return result being sent
	// back to the native Android caller.
	return arg1 + arg2;
}

/**
 * Call the native Android API function, `displayToast`, to have it to show the
 * text from the `toastMessage` component of internal.html in a native Android
 * toast.
 */
function displayToast() {
	console.warn("Warning a Toast is about to happen!");

	// noinspection JSUnresolvedFunction
	ExampleNativeAndroidAPI.displayToast(getToastMessage());
}

/**
 * Gets the inputted strings to be concatenated and passes it to the Android
 * native API, `ExampleNativeAndroidAPI.roundTripConcat`.
 */
function concatPresentedStrings() {
	// noinspection JSUnresolvedFunction
	ExampleNativeAndroidAPI.roundTripConcat(
		document.getElementById('concat1').value,
		document.getElementById('concat2').value);
}

/**
 * Call the native Android API function, `displayToast`, to have it to show the
 * text from the `toastMessage` component of internal.html in a native Android
 * toast.
 */
function checkFreshStart() {
	console.log("Calling checkFreshStart");
	let convId = context.nextConversationId();
	context.registerConversation(
		new AndroidAPIRequest(convId, MessageType.CheckFreshStart.name));
	// noinspection JSUnresolvedFunction
	ExampleNativeAndroidAPI.checkFreshStart(convId);
}

/**
 * Call the native Android API function, `displayToast`, to have it to show the
 * text from the `toastMessage` component of internal.html in a native Android
 * toast.
 */
function getFile() {
	console.log("Calling getFile");
	let convId = context.nextConversationId();
	context.registerConversation(
		new AndroidAPIRequest(convId, MessageType.GetFile.name));
	// noinspection JSUnresolvedFunction
	ExampleNativeAndroidAPI.getFile(convId, "sample-file.txt");
}

/**
 * Call the native Android API function, `displayToast`, to have it to show the
 * text from the `toastMessage` component of internal.html in a native Android
 * toast.
 */
function getFileList() {
	console.log("Calling getFileList");
	let convId = context.nextConversationId();
	context.registerConversation(
		new AndroidAPIRequest(convId, MessageType.GetFileList.name));
	// noinspection JSUnresolvedFunction
	ExampleNativeAndroidAPI.getFileList(convId);
}

/**
 * Open barcode scanner to scan a barcode. This navigates away from the web
 * app's WebView.
 */
function scanBarcode () {
	console.log("Calling scanBarcode");
	let convId = context.nextConversationId();
	context.registerConversation(
		new AndroidAPIRequest(convId, MessageType.GetBarcode.name));
	// noinspection JSUnresolvedFunction
	ExampleNativeAndroidAPI.openScanner(convId);
}

////////////////////////////////////////////////////////////////////////////////
//                    Native Android APIMessage Handling                      //
////////////////////////////////////////////////////////////////////////////////
/**
 * The object that statically contains all the message types and their handlers.
 * @type {{}}
 */
const MessageType = {
	Acknowledgement: {
		name: "Acknowledgement",
		process: (resp) => {
			(new AcknowledgementResponse(resp)).process();
		}
	},
	Error: {
		name: "Error",
		process: (resp) => {
			(new ErrorResponse(resp)).process();
		}
	},
	GetFile: {
		name: "GetFile",
		process: (resp) => {
			(new GetFileResponse(resp)).process();
		}
	},
	GetFileList: {
		name: "GetFileList",
		process: (resp) => {
			(new GetFileListResponse(resp)).process();
		}
	},
	GetBarcode: {
		name: "GetBarcode",
		process: (resp) => {
			(new GetBarcodeResponse(resp)).process();
		}
	},
	CheckFreshStart: {
		name: "CheckFreshStart",
		process: (resp) => {
			(new CheckFreshStartResponse(resp)).process();
		}
	}
};

/**
 * Represents an asynchronous request called into the native Android API.
 */
class AndroidAPIRequest {
	/**
	 * @param {number} conversationId
	 *   The positive integer that represents this instance of
	 *   {@link AndroidAPIRequest}.
	 * @param {string} messageType
	 *   The name that uniquely represents the request type that was made
	 *   of the Android native API.
	 */
	constructor(conversationId, messageType) {
		/**
		 * @property {number} conversationId
		 *   The positive integer that represents this instance of
		 *   {@link AndroidAPIRequest}.
		 */
		this.conversationId = conversationId;

		/**
		 * @property {string} messageType
		 *   The name that uniquely represents the request type that was made
		 *   of the Android native API. It dictates which
		 *   {@link AndroidAPIResponse} is used to process the response from
		 *   Android in the event the request is successful. This needs to
		 *   correspond with a field in {@link MessageType}.
		 */
		this.messageType = messageType;
	}
}

/**
 * A response to a native {@link AndroidAPIRequest}.
 */
class AndroidAPIResponse {
	/**
	 * The function to process this response's {@link AndroidAPIResponse#payload}.
	 *
	 * Note subclasses must override this method.
	 */
	process () {}

	/**
	 * Construct the {@link AndroidAPIResponse}.
	 *
	 * @param {number} conversationId
	 *   The positive integer that represents this instance of
	 *   {@link AndroidAPIResponse}.
	 * @param {string} messageType
	 *   The type of message. This dictates the structure of the
	 *   {@link payload}.
	 * @param {boolean} succeeded
	 *   `true` if the response was successful; `false` otherwise.
	 * @param {{}} payload
	 *   The data contained in this message. This is an object and
	 *   its contents are dependent on the {@link messageType}.
	 */
	constructor(
		conversationId,
		messageType,
		succeeded,
		payload
	) {
		/**
		 * @property {number} conversationId
		 *   The positive integer that represents this instance of
		 *   {@link AndroidAPIResponse}.
		 */
		this.conversationId = conversationId;

		/**
		 * @property {string} messageType
		 *   The type of message. This dictates the structure of the
		 *   {@link payload}.
		 */
		this.messageType = messageType;

		/**
		 * @property {{}} payload
		 *   The data contained in this message. This is an object and its
		 *   contents are dependent on the {@link messageType}.
		 */
		this.payload = payload;

		/**
		 * @property {boolean} succeeded
		 *   `true` if the response was successful; `false` otherwise.
		 */
		this.succeeded = succeeded;
	}
}

/**
 * A response acknowledging the action that needs no special action.
 */
// noinspection JSUnusedGlobalSymbols
class AcknowledgementResponse extends AndroidAPIResponse {
	/**
	 * @param {{}} raw
	 *   The raw JSON response.
	 */
	constructor(raw) {
		super(
			raw.conversationId,
			"Acknowledgement",
			raw.succeeded,
			raw.payload);
	}
}

/**
 * A response indicating an error occurred while handling the request.
 */
// noinspection JSUnusedGlobalSymbols
class ErrorResponse extends AndroidAPIResponse {
	process() {
		console.error(
			`(${this.conversationId}) ${this.messageType} failed:\n` +
				this.payload.errorMessage);
	}

	/**
	 * @param {{}} raw
	 *   The raw JSON response.
	 */
	constructor(raw) {
		super(
			raw.conversationId,
			MessageType.Error.name,
			raw.succeeded,
			raw.payload);

		/**
		 * @property {string} originatingMessageType
		 *   The message type name that represents the original request type
		 *   that this response is for.
		 */
		this.originatingMessageType = raw.payload.originatingMessageType;

		/**
		 * @property {string} errorMessage
		 *   The error message that describes how the request failed.
		 */
		this.errorMessage = raw.payload.errorMessage;
	}
}

/**
 * A response to getting the contents of a singular file.
 */
// noinspection JSUnusedGlobalSymbols
class GetFileResponse extends AndroidAPIResponse {
	process() {
		let fileContent = document.getElementById("fileContent");
		let node = document.createTextNode(this.payload.fileText);
		fileContent.appendChild(node);
	}

	/**
	 * @param {{}} raw
	 *   The raw JSON response.
	 */
	constructor(raw) {
		super(
			raw.conversationId,
			MessageType.GetFile.name,
			raw.succeeded,
			raw.payload);

		/**
		 * @property {string} fileText The text of the file.
		 */
		this.fileText = raw.payload.fileText;
	}
}

/**
 * A response to getting the contents the application's files directory.
 */
// noinspection JSUnusedGlobalSymbols
class GetFileListResponse extends AndroidAPIResponse {
	process() {
		alert(this.files.map((f) => `${f[0]}`).join('\n'));
	}

	/**
	 * @param {{}} raw
	 *   The raw JSON response.
	 */
	constructor(raw) {
		super(
			raw.conversationId,
			MessageType.GetFileList.name,
			raw.succeeded,
			raw.payload);

		/**
		 * @property {(string|boolean)[][]} files
		 *   The array of pairs of path relative files and a boolean, true
		 *   indicates it is a directory or false if it is not a directory.
		 */
		this.files = raw.payload.files;
	}
}

/**
 * A response to scanning a barcode request.
 */
// noinspection JSUnusedGlobalSymbols
class GetBarcodeResponse extends AndroidAPIResponse {
	process() {
		alert(`Scanned Barcode Data: ${this.barcode}\n` +
			`Scanned Barcode Format: ${this.format}\n` +
			`Scanned Barcode Type: ${this.type}\n`);
	}

	/**
	 * @param {{}} raw
	 *   The raw JSON response.
	 */
	constructor(raw) {
		super(
			raw.conversationId,
			MessageType.GetBarcode.name,
			raw.succeeded,
			raw.payload);

		/**
		 * @property {string} barcode
		 *   The text stored in the barcode.
		 */
		this.barcode = raw.payload.barcode;

		/**
		 * @property {string} format
		 *   The format of the barcode scanned.
		 */
		this.format = raw.payload.format;

		/**
		 * @property {string} type
		 *   The type of the barcode scanned.
		 */
		this.type = raw.payload.type;
	}
}

/**
 * A response to the startup request to see if the Android app was launched
 * newly, or if this is a redraw of the screen.
 */
// noinspection JSUnusedGlobalSymbols
class CheckFreshStartResponse extends AndroidAPIResponse {
	process() {
		if (this.freshStart) {
			clearAppLocalStorage();
		}
		else {
			restoreState();
		}
		context.isProcessingMessages = true;
	}

	/**
	 * @param {{}} raw
	 *   The raw JSON response.
	 */
	constructor(raw) {
		super(
			raw.conversationId,
			MessageType.CheckFreshStart.name,
			raw.succeeded,
			raw.payload);

		/**
		 * @property {boolean} freshStart
		 *   `true` indicates this is a new load of the app; `false` is a redraw
		 *   of the screen.
		 */
		this.freshStart = raw.payload.freshStart;
	}
}