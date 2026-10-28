import json
import re
import html
from pathlib import Path
from urllib.parse import quote

SOURCE = Path('C:/Users/Admin/Downloads/苍穹外卖-管理端接口.json')
OUT = Path(__file__).resolve().parent
groups = json.loads(SOURCE.read_text(encoding='utf-8-sig'))
lines = []
def add(s=''):
    lines.append(s)
def cell(v):
    return str(v if v is not None else '—').replace('|', '&#124;').replace('\r', '').replace('\n', '<br>')
def table(headers, rows):
    add('| ' + ' | '.join(headers) + ' |')
    add('| ' + ' | '.join(['---'] * len(headers)) + ' |')
    for row in rows:
        add('| ' + ' | '.join(cell(v) for v in row) + ' |')
    add()
def parse(raw):
    if not raw:
        return None
    try:
        return json.loads(raw)
    except (ValueError, TypeError):
        return None
def typ(s):
    t = s.get('type', '未注明')
    if t == 'array':
        t = 'array<' + typ(s.get('items', {})) + '>'
    if s.get('format'):
        t += ' (' + s['format'] + ')'
    return t
def fields(s, prefix='', parent_required=True):
    rows = []
    required = s.get('required', [])
    for key, prop in s.get('properties', {}).items():
        path = prefix + key
        req = '是' if key in required else '未标必填'
        if key in required and prefix:
            req = '父对象存在时必填'
        notes = [prop.get('description') or '未注明']
        for attr, label in [('enum', '枚举'), ('default', '默认值'), ('minimum', '最小值'), ('maximum', '最大值'), ('minLength', '最短长度'), ('maxLength', '最长长度'), ('pattern', '规则')]:
            if attr in prop:
                notes.append(label + '：' + json.dumps(prop[attr], ensure_ascii=False))
        rows.append(['`' + path + '`', typ(prop), req, '；'.join(notes)])
        nested = prop.get('items', {}) if prop.get('type') == 'array' else prop
        if nested.get('properties'):
            rows.extend(fields(nested, path + ('[].' if prop.get('type') == 'array' else '.')))
    return rows
def example(s):
    for key in ('example', 'default'):
        if key in s:
            return s[key]
    if s.get('enum'):
        return s['enum'][0]
    t = s.get('type')
    if t == 'object' or 'properties' in s:
        return {k: example(v) for k, v in s.get('properties', {}).items()}
    if t == 'array':
        return [example(s.get('items', {}))]
    if t in ('integer', 'number'):
        return s.get('minimum', 0)
    if t == 'boolean':
        return False
    if s.get('format') == 'date-time':
        return '2026-10-22T12:00:00'
    if s.get('format') == 'date':
        return '2026-10-22'
    return 'string'
def block(s, lang='json'):
    add('```' + lang)
    add(s)
    add('```')
    add()
def params(title, entries, path=False, header=False):
    if not entries:
        return
    add('#### ' + title)
    add()
    table(['参数', '类型', '必填', '说明', '原始示例 / 值'], [
        ['`' + p['name'] + '`', p.get('type') or '未注明', '是' if path or str(p.get('required')) == '1' else '未标必填', p.get('desc') or '未注明', p.get('value', p.get('example', '—'))] for p in entries])

total = sum(len(g.get('list', [])) for g in groups)
add('# 苍穹外卖 · 管理端接口文档')
add()
add(f'根据 `{SOURCE.name}` 整理 · 9 个模块 · {total} 个接口 · 整理日期：2026-10-22')
add()
add('## 阅读说明')
add()
add('- 本文仅依据所提供 JSON，未对运行中的服务进行验证。接口名称、路径、字段说明和必填标记均按原始定义保留。')
add('- 服务域名、端口及部署前缀未提供；请求示例以 `{{baseUrl}}` 作为占位。')
add('- 登录响应包含 JWT 令牌 `data.token`，但原文件未说明令牌的请求头名称、格式、有效期及哪些接口需鉴权。')
add('- “未标必填”表示原文件没有将该字段列为必填，并不保证服务端允许省略。嵌套字段的必填约束作用于其所在对象；路径参数必须提供。')
add('- Query 参数未注明类型时不推断类型；日期、状态、金额单位及批量参数规则以各接口原始说明为准，未注明项需联调确认。')
add('- JSON 示例由 Schema 自动生成，仅演示结构；`0`、`string` 等为占位值，不代表成功状态码或可提交的业务值。示例包含可选字段。')
add('- 原文件的接口状态均为 `undone`，不代表接口已实现或已验收。未提供 HTTP 状态码、完整业务错误码、权限及错误响应约定。')
add()
add('## 通用响应结构')
add()
add('大多数 JSON 响应包含以下字段，具体类型及嵌套结构见各接口。')
add()
table(['字段', '说明'], [['`code`', '整数；具体成功/失败取值原文件未提供'], ['`data`', '业务数据；类型随接口变化'], ['`msg`', '字符串；原文件未给出统一语义约定']])
add('## 接口目录')
add()
table(['模块', '数量', '跳转'], [[g['name'],len(g.get('list', [])), f"[查看](#module-{i})"] for i,g in enumerate(groups,1)])
for gi,g in enumerate(groups, 1):
    add(f'<a id="module-{gi}"></a>')
    add(f"## {gi}. {g['name']}")
    add()
    table(['编号', '接口', '方法', '路径'], [[f'{gi}.{ai}', f"[{a['title']}](#api-{gi}-{ai})", a['method'], '`'+a['path']+'`'] for ai,a in enumerate(g.get('list', []),1)])
    for ai,a in enumerate(g.get('list', []),1):
        add(f'<a id="api-{gi}-{ai}"></a>')
        add(f"### {gi}.{ai} {a['title']}")
        add()
        add(f"**{a['method']}** `{a['path']}`")
        add()
        params('路径参数',a.get('req_params',[]),path=True)
        params('查询参数（Query）',a.get('req_query',[]))
        params('请求头',a.get('req_headers',[]),header=True)
        req = parse(a.get('req_body_other')) if a.get('req_body_is_json_schema') else None
        add('#### 请求体')
        add()
        if req:
            add('格式：`application/json`。')
            add()
            if fields(req):
                table(['字段', '类型', '必填', '说明'], fields(req))
            block(json.dumps(example(req),ensure_ascii=False,indent=2))
        elif a.get('req_body_form'):
            params('表单字段', a['req_body_form'])
            add('使用 `multipart/form-data` 上传文件，由客户端自动生成 boundary。')
            add()
        elif a.get('req_body_other'):
            block(a['req_body_other'],'text')
        else:
            add('原文件未定义请求体。')
            add()
        add('#### 请求示例')
        add()
        url = '{{baseUrl}}' + a['path']
        for p in a.get('req_params',[]):
            url = url.replace('{'+p['name']+'}',quote(str(p.get('example') or 'VALUE'),safe=''))
        query = [quote(p['name'])+'='+quote(str(p.get('example') or 'VALUE'),safe=',') for p in a.get('req_query',[]) if str(p.get('required'))=='1' or p.get('example')]
        if query:
            url += '?'+'&'.join(query)
        request = a['method']+' '+url
        if req:
            request += '\nContent-Type: application/json\n\n'+json.dumps(example(req),ensure_ascii=False,indent=2)
        elif a.get('req_body_form'):
            request += '\nContent-Type: multipart/form-data; boundary=<客户端生成>\n\nfile: <待上传文件>'
        block(request, 'http')
        add('#### 响应字段')
        add()
        res = parse(a.get('res_body')) if a.get('res_body_is_json_schema') else None
        if res:
            if fields(res):
                table(['字段', '类型', '必填', '说明'], fields(res))
            add('结构示例（占位值）：')
            add()
            block(json.dumps(example(res),ensure_ascii=False,indent=2))
        else:
            add('原始响应类型：`'+str(a.get('res_body_type','未注明'))+'`。')
            add()
            block(a.get('res_body') or '未提供','text')
            if a['path']=='/admin/report/export':
                add('原文件仅提供 `OK`，未提供 Excel 文件流、文件名或响应头定义，实际下载行为需联调确认。')
                add()
        if a['path']=='/admin/setmeal':
            if a['method']=='DELETE':
                add('备注：`ids` 的批量编码格式在原文件中未注明。')
                add()
md='\n'.join(lines)+'\n'
OUT.mkdir(exist_ok=True)
(OUT/'苍穹外卖-管理端接口文档.md').write_text(md,encoding='utf-8')
try:
    import markdown
    body=markdown.markdown(md,extensions=['tables','fenced_code'])
except ImportError:
    def inline(s):
        s=html.escape(s)
        s=re.sub(r'`([^`]+)`',r'<code>\1</code>',s)
        s=re.sub(r'\*\*([^*]+)\*\*',r'<strong>\1</strong>',s)
        s=re.sub(r'\[([^\]]+)\]\(([^)]+)\)',r'<a href="\2">\1</a>',s)
        return s.replace('&lt;br&gt;','<br>').replace('&amp;#124;','&#124;')
    chunks=[]
    i=0
    while i<len(lines):
        line=lines[i]
        if line.startswith('```'):
            code=[]
            i+=1
            while i<len(lines) and not lines[i].startswith('```'):
                code.append(lines[i]); i+=1
            chunks.append('<pre><code>'+html.escape('\n'.join(code))+'</code></pre>')
        elif line.startswith('| '):
            rows=[]
            while i<len(lines) and lines[i].startswith('| '):
                rows.append([x.strip() for x in lines[i].strip('|').split('|')]); i+=1
            chunks.append('<table><thead><tr>'+''.join('<th>'+inline(c)+'</th>' for c in rows[0])+'</tr></thead><tbody>'+''.join('<tr>'+''.join('<td>'+inline(c)+'</td>' for c in r)+'</tr>' for r in rows[2:])+'</tbody></table>')
            continue
        elif line.startswith('<a id='):
            chunks.append(line)
        elif line.startswith('#'):
            level=len(line)-len(line.lstrip('#'))
            chunks.append(f'<h{level}>'+inline(line[level:].strip())+f'</h{level}>')
        elif line.startswith('- '):
            items=[]
            while i<len(lines) and lines[i].startswith('- '):
                items.append('<li>'+inline(lines[i][2:])+'</li>'); i+=1
            chunks.append('<ul>'+''.join(items)+'</ul>')
            continue
        elif line:
            chunks.append('<p>'+inline(line)+'</p>')
        i+=1
    body='\n'.join(chunks)
nav=''.join(f'<a href="#module-{i}">{html.escape(g["name"])} <small>{len(g["list"])}</small></a>' for i,g in enumerate(groups,1))
page='''<!doctype html><html lang="zh-CN"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>苍穹外卖 · 管理端接口文档</title><style>
*{box-sizing:border-box}html{scroll-behavior:smooth}body{margin:0;color:#243247;background:#f5f7fb;font:15px/1.8 "Microsoft YaHei",system-ui,sans-serif}aside{position:fixed;width:240px;height:100vh;padding:30px 22px;background:#12243a;color:white;overflow:auto}aside strong{font-size:21px}aside p{color:#a8bed4}aside a{display:block;color:#d8e5f2;text-decoration:none;padding:9px 0}small{float:right;color:#8faac6}main{max-width:1250px;margin-left:240px;padding:40px 55px 90px}h1{font-size:32px;color:#102c49}h2{margin-top:55px;border-bottom:2px solid #cbd9e8;padding-bottom:12px}h3{margin-top:38px;color:#095782}h4{margin-bottom:12px}table{border-collapse:collapse;width:100%;background:white;font-size:14px;display:block;overflow:auto;margin:18px 0 25px}th,td{padding:10px 14px;border:1px solid #dce4ef;text-align:left;vertical-align:top}th{background:#e9f0f8;white-space:nowrap}a{color:#087aa3}code{font-family:Consolas,monospace;background:#eaf0f7;padding:2px 5px;border-radius:4px;overflow-wrap:anywhere}pre{padding:18px 22px;background:#14273d;color:#e6eef8;border-radius:8px;overflow:auto;line-height:1.6}pre code{background:transparent;padding:0;color:inherit}li{margin:8px 0}a[id]{scroll-margin-top:20px}@media(max-width:850px){aside{position:static;width:auto;height:auto}aside a{display:inline-block;margin-right:20px}main{margin:0;padding:20px}}@media print{aside{display:none}main{margin:0;padding:0}body{background:white}pre{white-space:pre-wrap;background:#f2f4f7;color:black}h3{break-after:avoid}tr{break-inside:avoid}a{color:inherit}}
</style><aside><strong>苍穹外卖</strong><p>管理端接口文档<br>9 个模块 · 47 个接口</p>'''+nav+'</aside><main>'+body+'</main></html>'
(OUT/'苍穹外卖-管理端接口文档.html').write_text(page,encoding='utf-8')
assert md.count('#### 响应字段')==total
assert md.count('#### 请求示例')==total
for gi,g in enumerate(groups,1):
    for ai,a in enumerate(g['list'],1):
        section=md.split(f'<a id="api-{gi}-{ai}"></a>')[1].split('<a id=')[0]
        for schema_key in ['req_body_other','res_body']:
            s=parse(a.get(schema_key))
            if isinstance(s,dict):
                for row in fields(s):
                    assert row[0] in section,(a['title'],row[0])
print(f'完成：{len(groups)} 个模块，{total} 个接口；所有 Schema 字段与请求示例覆盖检查通过。')
for f in OUT.glob('苍穹外卖-管理端接口文档.*'):
    print(str(f),f.stat().st_size)
