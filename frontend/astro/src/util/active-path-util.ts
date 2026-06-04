export function getClassNameForCurrentPath(
    className: string,
    path: string,
    currentPath: string,
): string {
    if (path === currentPath) {
        return className + " active";
    }
    return className;
}
