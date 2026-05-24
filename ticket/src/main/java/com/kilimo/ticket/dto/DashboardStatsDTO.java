package com.kilimo.ticket.dto;

public class DashboardStatsDTO {

    private Long totalTickets;
    private Long openTickets;
    private Long resolvedTickets;
    private Long slaBreaches;
    private Long totalUsers;

    public DashboardStatsDTO() {}

    public DashboardStatsDTO(Long totalTickets,
                             Long openTickets,
                             Long resolvedTickets,
                             Long slaBreaches,
                             Long totalUsers) {

        this.totalTickets = totalTickets;
        this.openTickets = openTickets;
        this.resolvedTickets = resolvedTickets;
        this.slaBreaches = slaBreaches;
        this.totalUsers = totalUsers;
    }

    public Long getTotalTickets() { return totalTickets; }

    public void setTotalTickets(Long totalTickets) { this.totalTickets = totalTickets; }

    public Long getOpenTickets() { return openTickets; }

    public void setOpenTickets(Long openTickets) { this.openTickets = openTickets; }

    public Long getResolvedTickets() { return resolvedTickets; }

    public void setResolvedTickets(Long resolvedTickets) { this.resolvedTickets = resolvedTickets; }

    public Long getSlaBreaches() { return slaBreaches; }

    public void setSlaBreaches(Long slaBreaches) { this.slaBreaches = slaBreaches; }

    public Long getTotalUsers() { return totalUsers; }

    public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }
}