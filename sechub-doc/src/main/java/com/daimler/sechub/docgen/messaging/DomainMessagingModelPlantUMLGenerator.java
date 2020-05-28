// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.messaging;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.docgen.Generator;
import com.daimler.sechub.docgen.GeneratorConstants;
import com.daimler.sechub.docgen.messaging.DomainMessagingModel.Domain;
import com.daimler.sechub.docgen.messaging.DomainMessagingModel.DomainPart;
import com.daimler.sechub.docgen.messaging.DomainMessagingModel.DomainPartMessageInfo;
import com.daimler.sechub.sharedkernel.messaging.IsReceivingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.IsSendingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.IsSendingSyncMessage;
import com.daimler.sechub.sharedkernel.messaging.IsSendingSyncMessageAnswer;
import com.daimler.sechub.sharedkernel.messaging.MessageID;

public class DomainMessagingModelPlantUMLGenerator implements Generator {

	private static final String EVENT_BUS_ID = "EventBus";

	private static final String MESSAGE_WITH_ID = "message with id ";

	private static final Logger LOG = LoggerFactory.getLogger(DomainMessagingModelPlantUMLGenerator.class);

	public String generate(DomainMessagingModel model, String title, MessageID[] messageIDs, boolean onlyWithGivenMessageIds) {
		Context context = new Context();
		context.model=model;
		
		context.addLine("@startuml");
		context.addLine("\n");
		context.addLine("title "+title);
		context.addLine("skinparam style strictuml");
		context.addLine("\n");
		context.addLine("control "+EVENT_BUS_ID);
		
		boolean alternate=false;
		for (Domain domain: model.domains) {
			alternate=!alternate;
			String data = "box \""+domain.name+"\" ";
			// see https://www.w3schools.com/colors/colors_names.asp
			if (alternate) {
				data+= "#C8C8CF";
			}else {
				data+= "#F8F8FF";//#Ghostwhite
			}
			context.addLine(data);//#LightBlue
			for (DomainPart part: domain.domainParts) {
				if (onlyWithGivenMessageIds && ! part.isHandlingAtLeastOneOf(messageIDs)) {
						continue;
				}
				context.addLine("   participant "+part.name);
			}
		    context.addLine("end box");
		}

		for (MessageID messageId : messageIDs) {
			context.messageId=messageId;
			debug("INSPECT:" + messageId);
			generateMessage(context);
		}
		context.addLine("\n");
		context.addLine("@enduml");

		return context.sb.toString();
	}

	private void debug(String string) {
		if (!GeneratorConstants.DEBUG) {
			return;
		}
		LOG.info(string);

	}
	
	private class Context{
		private Set<MessageID> unused = new HashSet<>();
		private DomainMessagingModel model;
		private MessageID messageId;
		private StringBuilder sb = new StringBuilder();
		private StringBuilder tryoutSb=new StringBuilder();
		private StringBuilder originSb;
		private void addLine(String text) {
			sb.append(text).append("\n");
		}
		public void markCurrentMessageIdAsUnused() {
			unused.add(messageId);
		}
		public void startTryoutMode() {
			this.originSb = sb;
			tryoutSb = new StringBuilder();
			sb=tryoutSb;
		}
		
		public void endTryoutMode() {
			this.sb=originSb;
		}
		
		public String getTryoutResult() {
			return tryoutSb.toString();
		}
	}

	private void generateMessage(Context context) {
		boolean isSendingSync = handleSync(context);
		boolean isSendingAsync = handleAsync(context);
		if (isSendingSync && isSendingAsync) {
			String message = MESSAGE_WITH_ID + context.messageId
					+ " is already send synchronous but is handled also async way?!?!?";
			handleValidationError(message);
		}else if (!isSendingAsync && !isSendingSync) {
			/* not handled at all*/
			context.markCurrentMessageIdAsUnused();
		}
		
		

	}

	private boolean handleAsync(Context context) {
		List<DomainPartMessageInfo<IsSendingAsyncMessage>> sendAsyncs = assertValid(
				context.model.createDomainPartMessageInfos(IsSendingAsyncMessage.class, context.messageId));
		List<DomainPartMessageInfo<IsReceivingAsyncMessage>> receiveAsync = assertValid(
				context.model.createDomainPartMessageInfos(IsReceivingAsyncMessage.class, context.messageId));
		context.startTryoutMode();
		/* asynchronous messaging */
		boolean asyncHandled = false;
		for (DomainPartMessageInfo<IsSendingAsyncMessage> sendAsync : sendAsyncs) {
			sendToEventBus(context.sb, sendAsync.part.name, sendAsync.messageId,false);
			asyncHandled = true;
		}
		for (DomainPartMessageInfo<IsReceivingAsyncMessage> receivedAsync : receiveAsync) {
			sendFromEventBus(context.sb, receivedAsync.part.name, receivedAsync.messageId.name(),false,false);
			asyncHandled = true;
		}
		context.endTryoutMode();
		if (asyncHandled) {
			context.addLine("autonumber 1");
			context.sb.append(context.getTryoutResult());
			context.addLine("autonumber stop");
		}
		return asyncHandled;
	}

	private boolean handleSync(Context context) {
		context.startTryoutMode();
		
		List<DomainPartMessageInfo<IsSendingSyncMessage>> sendSync = assertValid(
				context.model.createDomainPartMessageInfos(IsSendingSyncMessage.class, context.messageId), 1);

		List<DomainPartMessageInfo<IsSendingSyncMessageAnswer>> sendsSyncMessageAnswer = assertValid(
				context.model.createDomainPartMessageInfos(IsSendingSyncMessageAnswer.class, context.messageId), 1);
		boolean isSendingSync = false;
		/* synchronouos way */
		for (DomainPartMessageInfo<IsSendingSyncMessage> syncQuestion : sendSync) {
			sendToEventBus(context.sb, syncQuestion.part.name, syncQuestion.messageId,true);
			isSendingSync = true;
		}
		boolean isAnsweringSync = handleSendSynchronMessageIntoEventBus(sendsSyncMessageAnswer, context);
		handleSendSynchronMessageBackFromEventBus(context, sendSync, isSendingSync, isAnsweringSync);

		context.endTryoutMode();
		if (isSendingSync) {
			context.addLine("autonumber 1");
			context.sb.append(context.getTryoutResult());
			context.addLine("autonumber stop");
		}

		return isSendingSync;
	}

	private boolean handleSendSynchronMessageIntoEventBus(
			List<DomainPartMessageInfo<IsSendingSyncMessageAnswer>> sendsSyncMessageAnswer, Context context) {
		boolean isAnsweringSync = false;
		boolean syncFromEventBusDone = false;
		int maxBranch = sendsSyncMessageAnswer.size();
		boolean branches = maxBranch > 1;
		int branchCount = 0;
		for (DomainPartMessageInfo<IsSendingSyncMessageAnswer> syncAnswer : sendsSyncMessageAnswer) {
			if (!syncFromEventBusDone) {
				sendFromEventBus(context.sb, syncAnswer.part.name, syncAnswer.answeringTo.name(),true,false);
				syncFromEventBusDone = true;
			}
			if (branches) {
				if (branchCount == 0) {
					context.addLine("alt " + syncAnswer.branchName);
				} else {
					context.addLine("else " + syncAnswer.branchName);
				}
				branchCount++;
			}
			sendToEventBus(context.sb, syncAnswer.part.name, syncAnswer.messageId,true);
			if (branches && maxBranch == branchCount) {
				context.addLine("end");
			}
			isAnsweringSync = true;
		}
		return isAnsweringSync;
	}

	private void handleSendSynchronMessageBackFromEventBus(Context context, List<DomainPartMessageInfo<IsSendingSyncMessage>> sendSync, boolean isSendingSync,
			boolean isAnsweringSync) {
		if (isAnsweringSync && isSendingSync) {
			for (DomainPartMessageInfo<IsSendingSyncMessage> syncQuestion : sendSync) {
				sendFromEventBus(context.sb, syncQuestion.part.name, ""/*"result of:" + syncQuestion.messageId*/,true,true);
			}
		} else {
			if (isAnsweringSync) {
				String message = MESSAGE_WITH_ID + context.messageId + " is answered for sync, but nobody sends it !?!?";
				handleValidationError(message);
			} else if (isSendingSync) {
				String message = MESSAGE_WITH_ID + context.messageId + " is send sync, but nobody answers it !?!?";
				handleValidationError(message);
			} else {
				/* just ignore. if so the message is just not handled synchron but maybe only asynchronus*/
			}
		}
	}

	private void handleValidationError(String message) {
		throw new IllegalStateException("NOT VALID:"+message);
	}

	private void sendToEventBus(StringBuilder sb, String sender, MessageID messageID, boolean synchron) {
		sb.append(sender);
		sb.append(" -");
		if (!synchron) {
			sb.append(">>");
		}else {
			sb.append(">");
		}
		sb.append(" ").append(EVENT_BUS_ID).append(":").append(messageID.name()).append("\n");
	}

	private void sendFromEventBus(StringBuilder sb, String name, String end, boolean synchron, boolean backward) {
		sb.append(EVENT_BUS_ID);
		sb.append(" ");
		
		if (backward) {
			sb.append("-");
		}
		sb.append("-");
		if (!synchron) {
			sb.append(">>");
		}else {
			sb.append(">");
		}
		sb.append(" ").append(name).append(":").append(end).append("\n");
	}

	private <T extends Annotation> List<DomainPartMessageInfo<T>> assertValid(List<DomainPartMessageInfo<T>> list) {
		return assertValid(list, -1);
	}

	private <T extends Annotation> List<DomainPartMessageInfo<T>> assertValid(List<DomainPartMessageInfo<T>> list,
			int maximum) {
		int size = list.size();
		if (size == 0) {
			return list;
		}
		if (maximum > size) {
			MessageID messageId = list.iterator().next().messageId;
			String message = "too many calls for " + messageId + " max=" + maximum + ", reached:" + size;
			handleValidationError(message);
		}
		return list;
	}

	

}
