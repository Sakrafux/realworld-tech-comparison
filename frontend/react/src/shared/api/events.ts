export const AUTH_EVENT = "auth:unauthorized";

export const triggerLogout = () => {
    window.dispatchEvent(new Event(AUTH_EVENT));
};
