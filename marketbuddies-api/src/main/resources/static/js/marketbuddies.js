// MarketBuddies: comportamento comum das telas (sem dependências).
document.addEventListener('DOMContentLoaded', () => {

  // Mostrar/ocultar senha: <div class="mb-password"><input type="password"><button type="button">…</button></div>
  document.querySelectorAll('.mb-password').forEach((wrapper) => {
    const input = wrapper.querySelector('input');
    const button = wrapper.querySelector('button');
    if (!input || !button) return;
    button.addEventListener('click', () => {
      const show = input.type === 'password';
      input.type = show ? 'text' : 'password';
      const label = show ? 'Ocultar senha' : 'Mostrar senha';
      button.setAttribute('aria-label', label);
      button.setAttribute('title', label);
    });
  });

  // <dialog data-aberto="true"> abre sozinho (ex.: erro de validação re-renderiza o modal aberto).
  document.querySelectorAll('dialog[data-aberto="true"]').forEach((dialog) => dialog.showModal());

  // Abrir: <button data-dialog-open="id-do-dialog">. Fechar: <button data-dialog-close> dentro do dialog.
  document.querySelectorAll('[data-dialog-open]').forEach((trigger) => {
    trigger.addEventListener('click', () => {
      const dialog = document.getElementById(trigger.dataset.dialogOpen);
      if (dialog) dialog.showModal();
    });
  });
  document.querySelectorAll('[data-dialog-close]').forEach((trigger) => {
    trigger.addEventListener('click', () => trigger.closest('dialog')?.close());
  });

  // Menu da navbar no celular.
  document.querySelectorAll('.mb-navbar__menu').forEach((button) => {
    button.addEventListener('click', () => {
      const navbar = button.closest('.mb-navbar');
      const open = navbar.classList.toggle('is-open');
      button.setAttribute('aria-expanded', String(open));
    });
  });
});
