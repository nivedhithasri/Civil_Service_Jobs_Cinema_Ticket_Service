package uk.gov.dwp.uc.pairtest.validator;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketRequestValidator {

    private static final int MAX_TICKETS = 25;

    public void validate(Long accountId, TicketTypeRequest... ticketTypeRequests) {
        validateAccount(accountId);
        validateTickets(ticketTypeRequests);
    }

    private void validateAccount(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException("Invalid account ID: " + accountId);
        }
    }

    private void validateTickets(TicketTypeRequest... ticketTypeRequests) {
        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException("At least one ticket must be purchased.");
        }

        int totalTickets = 0;
        int adultCount = 0;
        int childCount = 0;
        int infantCount = 0;

        for (TicketTypeRequest request : ticketTypeRequests) {
            if (request == null || request.getNoOfTickets() <= 0) {
                throw new InvalidPurchaseException("Each ticket type must have at least one ticket.");
            }

            totalTickets += request.getNoOfTickets();
            switch (request.getTicketType()) {
                case ADULT:
                    adultCount += request.getNoOfTickets();
                    break;
                case CHILD:
                    childCount += request.getNoOfTickets();
                    break;
                case INFANT:
                    infantCount += request.getNoOfTickets();
                    break;
            }
        }

        if (totalTickets > MAX_TICKETS) {
            throw new InvalidPurchaseException("A maximum of " + MAX_TICKETS + " tickets can be purchased at a time.");
        }

        if (adultCount == 0 && (childCount > 0 || infantCount > 0)) {
            throw new InvalidPurchaseException("At least one Adult ticket is required when purchasing Child or Infant tickets.");
        }
    }
}
