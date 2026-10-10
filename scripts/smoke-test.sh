#!/usr/bin/env bash
# Verificacao pos-deploy.
# Uso: scripts/smoke-test.sh <url-base> [versao-esperada]
#  - espera /actuator/health ficar UP
#  - se a versao (commit) for informada, espera /api/info mostrar essa versao
#    (evita aprovar a versao antiga que ainda estava no ar durante o deploy)
#  - confere os endpoints principais
# Variaveis opcionais: SMOKE_TIMEOUT (segundos, padrao 300), SMOKE_INTERVAL (padrao 10)
set -euo pipefail

BASE_URL="${1:?informe a URL base, ex.: http://localhost:8080}"
BASE_URL="${BASE_URL%/}"
VERSAO_ESPERADA="${2:-}"
TIMEOUT="${SMOKE_TIMEOUT:-300}"
INTERVALO="${SMOKE_INTERVAL:-10}"

pronto() {
  local health info
  health=$(curl -fsS --max-time 10 "$BASE_URL/actuator/health" 2>/dev/null) || return 1
  grep -q '"status":"UP"' <<<"$health" || return 1
  if [ -n "$VERSAO_ESPERADA" ]; then
    info=$(curl -fsS --max-time 10 "$BASE_URL/api/info" 2>/dev/null) || return 1
    grep -q "\"versao\":\"$VERSAO_ESPERADA\"" <<<"$info" || return 1
  fi
}

echo "Aguardando $BASE_URL ficar disponivel${VERSAO_ESPERADA:+ com a versao $VERSAO_ESPERADA} (ate ${TIMEOUT}s)..."
inicio=$SECONDS
until pronto; do
  if (( SECONDS - inicio >= TIMEOUT )); then
    echo "FALHA: a aplicacao nao ficou pronta em ${TIMEOUT}s." >&2
    curl -sS --max-time 10 "$BASE_URL/api/info" >&2 || true
    exit 1
  fi
  sleep "$INTERVALO"
done
echo "OK  health UP ($(( SECONDS - inicio ))s)"

falhas=0
verificar() {
  local caminho="$1" esperado="$2" corpo
  if corpo=$(curl -fsS --max-time 15 "$BASE_URL$caminho") && grep -q "$esperado" <<<"$corpo"; then
    echo "OK  GET $caminho"
  else
    echo "ERRO GET $caminho (esperado conter: $esperado)" >&2
    falhas=$((falhas + 1))
  fi
}

verificar /api/info '"ambiente"'
verificar /api/tipos 'VAZAMENTO_DE_AGUA'
verificar /api/ocorrencias '\['
verificar /api/transparencia '"porSecretaria"'

echo
curl -fsS "$BASE_URL/api/info"
echo

if (( falhas > 0 )); then
  echo "Smoke test FALHOU ($falhas verificacao(oes))." >&2
  exit 1
fi
echo "Smoke test OK."
