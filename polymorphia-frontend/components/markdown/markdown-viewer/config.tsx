import { Components } from "react-markdown";
import "./index.css";
import Image from "next/image";
import SyntaxHighlighter from "react-syntax-highlighter/dist/esm/light";
import { isValidUrl } from "@/components/markdown/isValidUrl";

import javascript from "react-syntax-highlighter/dist/esm/languages/hljs/javascript";
import typescript from "react-syntax-highlighter/dist/esm/languages/hljs/typescript";
import python from "react-syntax-highlighter/dist/esm/languages/hljs/python";
import java from "react-syntax-highlighter/dist/esm/languages/hljs/java";
import css from "react-syntax-highlighter/dist/esm/languages/hljs/css";
import bash from "react-syntax-highlighter/dist/esm/languages/hljs/bash";
import json from "react-syntax-highlighter/dist/esm/languages/hljs/json";
import sql from "react-syntax-highlighter/dist/esm/languages/hljs/sql";
import c from "react-syntax-highlighter/dist/esm/languages/hljs/c";
import { customTheme } from "@/components/markdown/markdown-viewer/customTheme";

SyntaxHighlighter.registerLanguage("javascript", javascript);
SyntaxHighlighter.registerLanguage("js", javascript);
SyntaxHighlighter.registerLanguage("typescript", typescript);
SyntaxHighlighter.registerLanguage("ts", typescript);
SyntaxHighlighter.registerLanguage("jsx", javascript);
SyntaxHighlighter.registerLanguage("tsx", typescript);
SyntaxHighlighter.registerLanguage("python", python);
SyntaxHighlighter.registerLanguage("java", java);
SyntaxHighlighter.registerLanguage("css", css);
SyntaxHighlighter.registerLanguage("bash", bash);
SyntaxHighlighter.registerLanguage("json", json);
SyntaxHighlighter.registerLanguage("sql", sql);
SyntaxHighlighter.registerLanguage("c", c);

export const markdownConfig: Components = {
  h1: ({ ...props }) => <h1 className="h1-markdown" {...props} />,
  h2: ({ ...props }) => <h2 className="h2-markdown" {...props} />,
  h3: ({ ...props }) => <h3 className="h3-markdown" {...props} />,
  h4: ({ ...props }) => <h4 className="h4-markdown" {...props} />,
  p: ({ ...props }) => <p className="p-markdown" {...props} />,
  ol: ({ ...props }) => <ol className="ol-markdown" {...props} />,
  ul: ({ ...props }) => <ul className="ul-markdown" {...props} />,
  li: ({ ...props }) => <li className="li-markdown" {...props} />,
  strong: ({ children, ...props }) => (
    <span className="strong-markdown" {...props}>
      {children}
    </span>
  ),
  em: ({ children, ...props }) => (
    <span className="em" {...props}>
      {children}
    </span>
  ),
  a: ({ children, ...props }) => (
    <a
      {...props}
      className="a-markdown"
      target="_blank"
      rel="noopener noreferrer"
    >
      {children}
    </a>
  ),
  img: ({ src, alt, ...props }) => {
    const { width: propWidth, height: propHeight } = props;
    const isInline = "data-inline" in props;

    const width = parseInt(String(propWidth), 10) || 900;
    const height = parseInt(String(propHeight), 10) || 600;

    if (typeof src !== "string" || !isValidUrl(src)) {
      return <span className="markdown-invalid-src">Invalid image url</span>;
    }

    if (isInline) {
      return (
        <span className="markdown-inline-image">
          <Image
            src={src!}
            alt={alt || ""}
            width={width}
            height={height}
            className="img-markdown-inline"
            quality={75}
          />
        </span>
      );
    }

    return (
      <div>
        <Image
          src={src!}
          alt={alt || ""}
          width={width}
          height={height}
          className="img-markdown"
          sizes="(max-width: 768px) 100vw, (max-width: 1200px) 80vw, 70vw"
          quality={75}
        />
      </div>
    );
  },
  table: ({ ...props }) => (
    <div className="table-markdown-wrapper">
      <table className="table-markdown" {...props} />
    </div>
  ),
  thead: ({ ...props }) => <thead className="thead-markdown" {...props} />,
  tbody: ({ ...props }) => <tbody {...props} />,
  tr: ({ ...props }) => <tr className="tr-markdown" {...props} />,
  th: ({ ...props }) => <th className="th-markdown" {...props} />,
  td: ({ ...props }) => <td className="td-markdown" {...props} />,
  code({ node, className, children, ...props }) {
    const isBlock = node?.position?.start.line !== node?.position?.end.line;
    const match = /language-(\w+)/.exec(className || "");

    if (!isBlock) {
      return (
        <code
          className="code-markdown-inline"
          style={{ fontFamily: "var(--league)" }}
          {...props}
        >
          {children}
        </code>
      );
    }

    return match ? (
      <SyntaxHighlighter
        PreTag="div"
        language={match[1]}
        style={customTheme}
        className="markdown-config-code-block"
      >
        {String(children).replace(/\n$/, "")}
      </SyntaxHighlighter>
    ) : (
      <pre className="markdown-config-code-block">
        <code className={`${className}`} {...props}>
          {children}
        </code>
      </pre>
    );
  },
};
