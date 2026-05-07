package org.enterprise.common.util;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TenantContext {

    private static final ThreadLocal<TenantContext> CONTEXT =
            new ThreadLocal<>();

    private Long userId;
    private Long companyId;
    private List<String> roles;

    private List<String> permissions;
    private List<Long> branchIds;
    private List<Long> warehouseIds;
    private List<Long> profitCenterIds;
    private List<Long> costCenterIds;

    public static void set(TenantContext context) {
        CONTEXT.set(context);
    }

    public static TenantContext get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static Long getCompanyId() {
        TenantContext ctx = CONTEXT.get();
        return ctx != null ? ctx.companyId : null;
    }

    public static Long getUserId() {
        TenantContext ctx = CONTEXT.get();
        return ctx != null ? ctx.userId : null;
    }
}