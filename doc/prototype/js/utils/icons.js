const { nextTick } = Vue;

export function refreshIcons() {
  nextTick(() => {
    lucide.createIcons();
  });
}
